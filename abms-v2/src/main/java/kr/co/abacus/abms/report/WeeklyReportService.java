package kr.co.abacus.abms.report;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.assistant.AssistantProperties;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.common.domain.NotFoundException;
import kr.co.abacus.abms.department.DepartmentRepository;
import kr.co.abacus.abms.department.DepartmentTree;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeRepository;
import kr.co.abacus.abms.party.Party;
import kr.co.abacus.abms.party.PartyRepository;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.project.ProjectAssignment;
import kr.co.abacus.abms.project.ProjectAssignmentRepository;
import kr.co.abacus.abms.project.ProjectRepository;
import kr.co.abacus.abms.project.ProjectRevenuePlan;
import kr.co.abacus.abms.project.ProjectRevenuePlanRepository;
import kr.co.abacus.abms.project.ProjectStatus;
import kr.co.abacus.abms.report.WeeklySnapshot.AssignmentLine;
import kr.co.abacus.abms.report.WeeklySnapshot.ProjectLine;
import kr.co.abacus.abms.report.WeeklySnapshot.RevenueLine;
import kr.co.abacus.abms.security.AccessService;
import kr.co.abacus.abms.security.DataScope;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.summary.MonthReport;
import kr.co.abacus.abms.summary.ProfitQueryService;

/**
 * 주간 운영 보고서 생성. AI 가 설정되어 있으면 LLM 으로 서술형 보고서를, 아니면 템플릿 보고서를 만든다.
 */
@Service
public class WeeklyReportService {

    private static final Logger log = LoggerFactory.getLogger(WeeklyReportService.class);

    private static final String PROMPT = """
            아래는 IT 서비스 회사의 프로젝트 손익 관리 시스템에서 추출한 주간 데이터입니다.
            이 데이터만 근거로 경영진에게 보고할 한국어 주간 운영 보고서를 마크다운으로 작성하세요.
            구성: ## 요약 (3줄 이내), ## 프로젝트 현황, ## 매출/청구, ## 인력 투입 변화, ## 리스크 및 확인 필요 사항.
            숫자는 데이터에 있는 값만 사용하고 금액은 천 단위 구분 기호와 '원'을 붙입니다. 데이터에 없는 내용은 만들지 마세요.

            {data}
            """;

    private final WeeklyReportRepository reportRepository;
    private final ProjectRepository projectRepository;
    private final ProjectRevenuePlanRepository revenuePlanRepository;
    private final ProjectAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final PartyRepository partyRepository;
    private final DepartmentRepository departmentRepository;
    private final ProfitQueryService profitQueryService;
    private final AccessService accessService;
    private final AssistantProperties aiProperties;
    private final ObjectProvider<ChatClient.Builder> chatClientBuilder;

    public WeeklyReportService(WeeklyReportRepository reportRepository, ProjectRepository projectRepository,
                               ProjectRevenuePlanRepository revenuePlanRepository,
                               ProjectAssignmentRepository assignmentRepository, EmployeeRepository employeeRepository,
                               PartyRepository partyRepository, DepartmentRepository departmentRepository,
                               ProfitQueryService profitQueryService, AccessService accessService,
                               AssistantProperties aiProperties, ObjectProvider<ChatClient.Builder> chatClientBuilder) {
        this.reportRepository = reportRepository;
        this.projectRepository = projectRepository;
        this.revenuePlanRepository = revenuePlanRepository;
        this.assignmentRepository = assignmentRepository;
        this.employeeRepository = employeeRepository;
        this.partyRepository = partyRepository;
        this.departmentRepository = departmentRepository;
        this.profitQueryService = profitQueryService;
        this.accessService = accessService;
        this.aiProperties = aiProperties;
        this.chatClientBuilder = chatClientBuilder;
    }

    @Transactional(readOnly = true)
    public Page<WeeklyReport> list(Pageable pageable) {
        return reportRepository.findAllByOrderByWeekStartDescIdDesc(pageable);
    }

    @Transactional(readOnly = true)
    public WeeklyReport get(Long id) {
        return reportRepository.findById(id).orElseThrow(() -> NotFoundException.of("주간 보고서", id));
    }

    public static LocalDate mondayOf(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public WeeklyReport generate(LoginUser user, LocalDate anyDayOfWeek) {
        accessService.require(user, PermissionCode.REPORT_READ);
        LocalDate weekStart = mondayOf(anyDayOfWeek);
        if (weekStart.isAfter(LocalDate.now())) {
            throw new BusinessException("미래 주차의 보고서는 만들 수 없습니다.");
        }
        WeeklySnapshot snapshot = snapshot(user, weekStart);
        String templateReport = WeeklyReportTemplate.render(snapshot);

        String content = templateReport;
        WeeklyReport.Generator generator = WeeklyReport.Generator.TEMPLATE;
        ChatClient.Builder builder = chatClientBuilder.getIfAvailable();
        if (aiProperties.isConfigured() && builder != null) {
            try {
                String aiReport = builder.build().prompt()
                        .user(u -> u.text(PROMPT).param("data", templateReport))
                        .call()
                        .content();
                if (aiReport != null && !aiReport.isBlank()) {
                    content = aiReport;
                    generator = WeeklyReport.Generator.AI;
                }
            } catch (RuntimeException e) {
                log.warn("AI 주간 보고서 생성 실패, 템플릿 보고서로 대체합니다: {}", e.getMessage());
            }
        }
        WeeklyReport report = WeeklyReport.create(user.accountId(), weekStart, content, generator);
        return reportRepository.save(report);
    }

    @Transactional
    public void edit(Long id, String title, String content) {
        get(id).edit(title, content);
    }

    @Transactional
    public void delete(LoginUser user, Long id) {
        get(id).softDelete(user.accountId());
    }

    @Transactional(readOnly = true)
    public WeeklySnapshot snapshot(LoginUser user, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        DataScope scope = accessService.scopeOf(user, PermissionCode.PROJECT_READ);
        List<Project> projects = projectRepository.findAllInScope(scope.all(), scope.departmentIds(), scope.projectIds());
        Map<Long, Project> projectById = projects.stream().collect(Collectors.toMap(Project::id, Function.identity()));
        Map<Long, String> partyNames = partyRepository.findAllById(projects.stream().map(Project::getPartyId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(Party::id, Party::getName));
        DepartmentTree tree = new DepartmentTree(departmentRepository.findAll());
        Map<Long, List<ProjectAssignment>> assignmentsByProject = assignmentRepository.findOverlapping(weekStart, weekEnd).stream()
                .filter(a -> projectById.containsKey(a.getProjectId()))
                .collect(Collectors.groupingBy(ProjectAssignment::getProjectId));

        List<ProjectLine> active = projects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.IN_PROGRESS || p.getPeriod().overlaps(weekStart, weekEnd) && !p.getStatus().isClosed())
                .sorted(Comparator.comparing(Project::getName))
                .map(p -> new ProjectLine(p.getCode(), p.getName(), p.getStatus().label(), partyNames.getOrDefault(p.getPartyId(), "-"),
                        tree.nameOf(p.getLeadDepartmentId()), p.getPeriod().startDate(), p.getPeriod().endDate(),
                        p.getContractAmount(), assignmentsByProject.getOrDefault(p.id(), List.of()).size()))
                .toList();

        List<RevenueLine> issued = revenuePlanRepository.findIssuedBetween(weekStart, weekEnd).stream()
                .filter(r -> projectById.containsKey(r.getProjectId()))
                .map(r -> toLine(r, projectById))
                .toList();
        List<RevenueLine> upcoming = projectById.isEmpty() ? List.of()
                : revenuePlanRepository.findUnissuedBetween(projectById.keySet(), weekEnd.plusDays(1), weekEnd.plusDays(14)).stream()
                .map(r -> toLine(r, projectById)).toList();
        List<RevenueLine> overdue = projectById.isEmpty() ? List.of()
                : revenuePlanRepository.findUnissuedBetween(projectById.keySet(), weekStart.minusYears(1), weekEnd).stream()
                .map(r -> toLine(r, projectById)).toList();

        List<AssignmentLine> changes = new ArrayList<>();
        Map<Long, String> employeeNames = employeeRepository.findAllById(assignmentsByProject.values().stream().flatMap(List::stream)
                        .map(ProjectAssignment::getEmployeeId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(Employee::id, Employee::getName));
        for (ProjectAssignment a : assignmentsByProject.values().stream().flatMap(List::stream).toList()) {
            String projectName = projectById.get(a.getProjectId()).getName();
            String employeeName = employeeNames.getOrDefault(a.getEmployeeId(), "-");
            if (!a.getPeriod().startDate().isBefore(weekStart)) {
                changes.add(new AssignmentLine(projectName, employeeName, "투입 시작", a.getPeriod().startDate()));
            }
            LocalDate end = a.getPeriod().endDate();
            if (end != null && !end.isAfter(weekEnd)) {
                changes.add(new AssignmentLine(projectName, employeeName, "투입 종료", end));
            }
        }
        changes.sort(Comparator.comparing(AssignmentLine::date));

        MonthReport month = user.has(PermissionCode.DASHBOARD_READ)
                ? profitQueryService.monthReport(user, YearMonth.from(weekStart)) : null;
        return new WeeklySnapshot(weekStart, weekEnd, active, issued, upcoming, overdue, changes,
                month == null ? Money.ZERO : month.revenue(), month == null ? Money.ZERO : month.cost(),
                month != null && month.calculated());
    }

    private static RevenueLine toLine(ProjectRevenuePlan r, Map<Long, Project> projects) {
        return new RevenueLine(projects.get(r.getProjectId()).getName(), r.getSequence(), r.getType().label(), r.getRevenueDate(), r.getAmount());
    }

}
