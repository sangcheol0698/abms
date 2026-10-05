package kr.co.abacus.abms.staffing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.department.DepartmentService;
import kr.co.abacus.abms.department.DepartmentTree;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeJob;
import kr.co.abacus.abms.employee.EmployeeRepository;
import kr.co.abacus.abms.employee.EmployeeStatus;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.project.ProjectAssignment;
import kr.co.abacus.abms.project.ProjectAssignmentRepository;
import kr.co.abacus.abms.project.ProjectRepository;
import kr.co.abacus.abms.site.Site;
import kr.co.abacus.abms.site.SiteRepository;

/**
 * 가용 인력 찾기: 기간 동안 월별 투입 M/M 을 합산해 평균 투입률과 가용 M/M 을 구하고, 조건에 맞는 재직자를 가용이 많은 순으로 보여준다.
 */
@Service
@Transactional(readOnly = true)
public class AvailabilityService {

    static final int MAX_MONTHS = 12;

    private final EmployeeRepository employeeRepository;
    private final ProjectAssignmentRepository assignmentRepository;
    private final ProjectRepository projectRepository;
    private final DepartmentService departmentService;
    private final SiteRepository siteRepository;

    public AvailabilityService(EmployeeRepository employeeRepository, ProjectAssignmentRepository assignmentRepository,
                               ProjectRepository projectRepository, DepartmentService departmentService, SiteRepository siteRepository) {
        this.employeeRepository = employeeRepository;
        this.assignmentRepository = assignmentRepository;
        this.projectRepository = projectRepository;
        this.departmentService = departmentService;
        this.siteRepository = siteRepository;
    }

    public List<Candidate> search(Criteria criteria, LocalDate today) {
        YearMonth from = criteria.from();
        YearMonth to = criteria.to();
        if (to.isBefore(from)) {
            throw new BusinessException("기간의 끝은 시작보다 늦어야 합니다.");
        }
        List<YearMonth> months = new ArrayList<>();
        for (YearMonth m = from; !m.isAfter(to); m = m.plusMonths(1)) {
            months.add(m);
        }
        if (months.size() > MAX_MONTHS) {
            throw new BusinessException("기간은 최대 " + MAX_MONTHS + "개월까지 볼 수 있습니다.");
        }
        DepartmentTree tree = departmentService.tree();
        Map<Long, Site> sites = siteRepository.findAll().stream().collect(Collectors.toMap(Site::id, Function.identity()));
        List<String> skills = criteria.skillList();

        List<Employee> employees = employeeRepository.findAllByStatusAndDeletedFalse(EmployeeStatus.ACTIVE).stream()
                .filter(e -> criteria.job() == null || criteria.job() == e.getJob())
                .filter(e -> criteria.minCareerYears() == null || e.careerMonths(today) >= criteria.minCareerYears() * 12L)
                .filter(e -> criteria.siteId() == null || criteria.siteId().equals(tree.siteIdOf(e.getDepartmentId())))
                .filter(e -> skills.isEmpty() || hasAllSkills(e, skills))
                .toList();

        LocalDate start = from.atDay(1);
        LocalDate end = to.atEndOfMonth();
        Map<Long, List<ProjectAssignment>> assignments = assignmentRepository.findOverlapping(start, end).stream()
                .collect(Collectors.groupingBy(ProjectAssignment::getEmployeeId));
        Map<Long, Project> projects = projectRepository.findAllById(assignments.values().stream().flatMap(List::stream)
                .map(ProjectAssignment::getProjectId).collect(Collectors.toSet())).stream().collect(Collectors.toMap(Project::id, Function.identity()));

        BigDecimal monthCount = BigDecimal.valueOf(months.size());
        List<Candidate> result = new ArrayList<>();
        for (Employee e : employees) {
            List<ProjectAssignment> mine = assignments.getOrDefault(e.id(), List.of());
            List<BigDecimal> monthly = months.stream()
                    .map(m -> mine.stream().map(a -> a.manMonth(m)).reduce(BigDecimal.ZERO, BigDecimal::add))
                    .toList();
            BigDecimal load = monthly.stream().reduce(BigDecimal.ZERO, BigDecimal::add).divide(monthCount, 2, RoundingMode.HALF_UP);
            BigDecimal peak = monthly.stream().max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
            BigDecimal free = BigDecimal.ONE.subtract(load).max(BigDecimal.ZERO);
            if (criteria.onlyAvailable() && free.signum() == 0) {
                continue;
            }
            Long siteId = tree.siteIdOf(e.getDepartmentId());
            List<String> projectNames = mine.stream().map(a -> projects.get(a.getProjectId())).filter(java.util.Objects::nonNull)
                    .map(Project::getName).distinct().toList();
            result.add(new Candidate(e, tree.nameOf(e.getDepartmentId()), siteId == null ? null : sites.get(siteId), load, peak, free,
                    monthly, projectNames, e.careerMonths(today)));
        }
        result.sort(Comparator.comparing(Candidate::free).reversed()
                .thenComparing(Comparator.comparingLong(Candidate::careerMonths).reversed())
                .thenComparing(c -> c.employee().getName()));
        return result;
    }

    private static boolean hasAllSkills(Employee employee, List<String> skills) {
        String owned = employee.getSkills() == null ? "" : employee.getSkills().toLowerCase(Locale.ROOT);
        return skills.stream().allMatch(owned::contains);
    }

    /**
     * @param skills 쉼표로 구분, 모두 포함해야 한다 (대소문자 무시, 부분 일치)
     */
    public record Criteria(YearMonth from, YearMonth to, @Nullable EmployeeJob job, @Nullable String skills, @Nullable Integer minCareerYears,
                           @Nullable Long siteId, boolean onlyAvailable) {

        List<String> skillList() {
            if (skills == null || skills.isBlank()) {
                return List.of();
            }
            return java.util.Arrays.stream(skills.split(",")).map(s -> s.trim().toLowerCase(Locale.ROOT)).filter(s -> !s.isEmpty()).toList();
        }

    }

    /**
     * @param load    기간 평균 투입 M/M (1.0 = 한 달 전부 투입)
     * @param peak    기간 중 가장 바쁜 달의 투입 M/M
     * @param free    평균 가용 M/M (1 - load, 0 이상)
     * @param monthly 월별 투입 M/M
     */
    public record Candidate(Employee employee, String departmentName, @Nullable Site site, BigDecimal load, BigDecimal peak, BigDecimal free,
                            List<BigDecimal> monthly, List<String> projects, long careerMonths) {

        public int loadPercent() {
            return Math.min(100, load.multiply(BigDecimal.valueOf(100)).intValue());
        }

        public boolean overloaded() {
            return peak.compareTo(BigDecimal.ONE) > 0;
        }

    }

}
