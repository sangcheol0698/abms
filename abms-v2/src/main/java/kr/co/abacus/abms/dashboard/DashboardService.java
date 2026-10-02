package kr.co.abacus.abms.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.employee.EmployeeRepository;
import kr.co.abacus.abms.employee.EmployeeStatus;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.project.ProjectRepository;
import kr.co.abacus.abms.project.ProjectRevenuePlan;
import kr.co.abacus.abms.project.ProjectRevenuePlanRepository;
import kr.co.abacus.abms.project.ProjectStatus;
import kr.co.abacus.abms.security.DataScope;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.summary.CompanyMonthlyCostSummary;
import kr.co.abacus.abms.summary.ProfitQueryService;
import kr.co.abacus.abms.summary.ProfitQueryService.MonthPoint;
import kr.co.abacus.abms.summary.ProfitRow;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final int UPCOMING_DAYS = 60;

    private final ProfitQueryService profitQueryService;
    private final ProjectRepository projectRepository;
    private final ProjectRevenuePlanRepository revenuePlanRepository;
    private final EmployeeRepository employeeRepository;

    public DashboardService(ProfitQueryService profitQueryService, ProjectRepository projectRepository,
                            ProjectRevenuePlanRepository revenuePlanRepository, EmployeeRepository employeeRepository) {
        this.profitQueryService = profitQueryService;
        this.projectRepository = projectRepository;
        this.revenuePlanRepository = revenuePlanRepository;
        this.employeeRepository = employeeRepository;
    }

    public Dashboard load(LoginUser user, int year) {
        DataScope scope = profitQueryService.scope(user);
        List<MonthPoint> trend = profitQueryService.yearlyTrend(user, year);
        Money revenue = trend.stream().map(MonthPoint::revenue).reduce(Money.ZERO, Money::plus);
        Money cost = trend.stream().map(MonthPoint::cost).reduce(Money.ZERO, Money::plus);

        List<Project> projects = projectRepository.findAll().stream()
                .filter(p -> scope.coversProject(p.id(), p.getLeadDepartmentId()))
                .toList();
        Map<ProjectStatus, Long> statusCounts = new EnumMap<>(ProjectStatus.class);
        for (ProjectStatus status : ProjectStatus.values()) {
            statusCounts.put(status, 0L);
        }
        projects.forEach(p -> statusCounts.merge(p.getStatus(), 1L, Long::sum));
        Money inProgressContract = projects.stream()
                .filter(p -> p.getStatus() == ProjectStatus.IN_PROGRESS)
                .map(Project::getContractAmount)
                .reduce(Money.ZERO, Money::plus);

        Map<Long, Project> projectById = projects.stream().collect(Collectors.toMap(Project::id, Function.identity()));
        LocalDate today = LocalDate.now();
        List<UpcomingRevenue> upcoming = projectById.isEmpty() ? List.of()
                : revenuePlanRepository.findUnissuedBetween(projectById.keySet(), today.minusDays(30), today.plusDays(UPCOMING_DAYS)).stream()
                .map(plan -> new UpcomingRevenue(plan, projectById.get(plan.getProjectId()), plan.getRevenueDate().isBefore(today)))
                .limit(8)
                .toList();

        Money unallocated = null;
        if (scope.all()) {
            unallocated = profitQueryService.companyCosts(year).stream()
                    .map(CompanyMonthlyCostSummary::getUnallocatedFullTimeCost)
                    .reduce(Money.ZERO, Money::plus);
        }
        long activeEmployees = employeeRepository.findAllByStatusAndDeletedFalse(EmployeeStatus.ACTIVE).size();

        List<ProfitRow> topProjects = profitQueryService.yearlyProjects(user, year).stream().limit(5).toList();
        List<ProfitRow> departments = profitQueryService.yearlyDepartments(user, year);
        return new Dashboard(year, scope.all(), revenue, cost, trend, topProjects, departments, statusCounts,
                inProgressContract, upcoming, unallocated, activeEmployees);
    }

    public record Dashboard(
            int year,
            boolean companyWide,
            Money revenue,
            Money cost,
            List<MonthPoint> trend,
            List<ProfitRow> topProjects,
            List<ProfitRow> departments,
            Map<ProjectStatus, Long> projectStatusCounts,
            Money inProgressContractAmount,
            List<UpcomingRevenue> upcomingRevenues,
            @Nullable Money unallocatedCost,
            long activeEmployees
    ) {

        public Money profit() {
            return revenue.minus(cost);
        }

        public BigDecimal margin() {
            return ProfitRow.margin(revenue, cost);
        }

        public long inProgressCount() {
            return projectStatusCounts.getOrDefault(ProjectStatus.IN_PROGRESS, 0L);
        }

        /** Chart.js 데이터 (JSON) */
        public String chartJson() {
            StringBuilder labels = new StringBuilder();
            StringBuilder revenues = new StringBuilder();
            StringBuilder costs = new StringBuilder();
            StringBuilder profits = new StringBuilder();
            for (MonthPoint p : trend) {
                String sep = labels.isEmpty() ? "" : ",";
                labels.append(sep).append('"').append(p.month().getMonthValue()).append("월\"");
                revenues.append(sep).append(p.revenue().amount().toPlainString());
                costs.append(sep).append(p.cost().amount().toPlainString());
                profits.append(sep).append(p.profit().amount().toPlainString());
            }
            return "{\"labels\":[" + labels + "],\"revenue\":[" + revenues + "],\"cost\":[" + costs + "],\"profit\":[" + profits + "]}";
        }

    }

    public record UpcomingRevenue(ProjectRevenuePlan plan, Project project, boolean overdue) {
    }

}
