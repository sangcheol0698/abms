package kr.co.abacus.abms.application.dashboard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import kr.co.abacus.abms.application.dashboard.dto.DashboardDepartmentFinancialItem;
import kr.co.abacus.abms.application.dashboard.dto.DashboardEmployeeOverviewResponse;
import kr.co.abacus.abms.application.dashboard.dto.DashboardMonthlyFinancialItem;
import kr.co.abacus.abms.application.dashboard.dto.DashboardProjectOverviewResponse;
import kr.co.abacus.abms.application.dashboard.dto.DashboardSummaryResponse;
import kr.co.abacus.abms.application.dashboard.dto.DashboardUpcomingDeadlineItem;
import kr.co.abacus.abms.application.dashboard.inbound.DashboardFinder;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.party.PartyQueryService;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.summary.outbound.MonthlyRevenueSummaryRepository;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardQueryService implements DashboardFinder {

    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final PartyQueryService partyQueryService;
    private final MonthlyRevenueSummaryRepository monthlyRevenueSummaryRepository;

    @Override
    public DashboardSummaryResponse getDashboardSummary(int year) {
        int totalEmployeesCount = employeeRepository.countByDeletedFalse();
        int activeProjectsCount = projectRepository.countByStatusAndDeletedFalse(ProjectStatus.IN_PROGRESS);
        LocalDate startDate = LocalDate.of(year, Month.JANUARY, 1);
        LocalDate endDate = LocalDate.of(year, Month.DECEMBER, 31);
        int completedProjectsCount = projectRepository.countByStatusAndDeletedFalseAndPeriodEndDateBetween(
                ProjectStatus.COMPLETED,
                startDate,
                endDate
        );
        int newEmployeesCount = employeeRepository.countByJoinDateBetweenAndDeletedFalse(startDate, endDate);
        List<DashboardMonthlyFinancialItem> monthlyFinancials = getMonthlyFinancials(year);
        long yearRevenue = monthlyFinancials.stream()
                .mapToLong(DashboardMonthlyFinancialItem::revenue)
                .sum();
        long yearProfit = monthlyFinancials.stream()
                .mapToLong(DashboardMonthlyFinancialItem::profit)
                .sum();

        return new DashboardSummaryResponse(
                totalEmployeesCount,
                activeProjectsCount,
                completedProjectsCount,
                newEmployeesCount,
                yearRevenue,
                yearProfit
        );
    }

    @Override
    public List<DashboardUpcomingDeadlineItem> getUpcomingDeadlines(int limit) {
        if (limit <= 0) {
            return List.of();
        }

        LocalDate today = LocalDate.now();
        LocalDate deadlineBoundary = today.plusDays(30);
        List<Project> candidates = new ArrayList<>();
        candidates.addAll(projectRepository.findAllByStatusAndDeletedFalse(ProjectStatus.IN_PROGRESS));
        candidates.addAll(projectRepository.findAllByStatusAndDeletedFalse(ProjectStatus.ON_HOLD));

        return candidates.stream()
                .filter(project -> hasUpcomingDeadline(project, deadlineBoundary))
                .sorted(Comparator
                        .comparing((Project project) -> !getProjectEndDate(project).isBefore(today))
                        .thenComparing(this::getProjectEndDate))
                .limit(limit)
                .map(project -> toUpcomingDeadlineItem(project, today))
                .toList();
    }

    @Override
    public List<DashboardMonthlyFinancialItem> getMonthlyFinancials(int year) {
        List<MonthlyRevenueSummary> snapshots = getLatestProjectSnapshotsByMonth(year);
        Map<YearMonth, List<MonthlyRevenueSummary>> summariesByMonth = snapshots.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        summary -> YearMonth.from(summary.getSummaryDate()),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));

        List<DashboardMonthlyFinancialItem> result = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            YearMonth targetMonth = YearMonth.of(year, month);
            List<MonthlyRevenueSummary> monthlySummaries = summariesByMonth.getOrDefault(targetMonth, List.of());

            long revenue = monthlySummaries.stream()
                    .map(MonthlyRevenueSummary::getRevenueAmount)
                    .reduce(Money.zero(), Money::add)
                    .amount()
                    .longValue();
            long cost = monthlySummaries.stream()
                    .map(MonthlyRevenueSummary::getCostAmount)
                    .reduce(Money.zero(), Money::add)
                    .amount()
                    .longValue();
            long profit = monthlySummaries.stream()
                    .map(MonthlyRevenueSummary::getProfitAmount)
                    .reduce(Money.zero(), Money::add)
                    .amount()
                    .longValue();

            result.add(new DashboardMonthlyFinancialItem(
                    targetMonth.atDay(1),
                    revenue,
                    cost,
                    profit
            ));
        }

        return result;
    }

    @Override
    public List<DashboardDepartmentFinancialItem> getDepartmentFinancials(int year, int limit) {
        if (limit <= 0) {
            return List.of();
        }

        Map<Long, DepartmentFinancialAccumulator> totalsByDepartment = new LinkedHashMap<>();
        for (MonthlyRevenueSummary summary : getLatestProjectSnapshotsByMonth(year)) {
            totalsByDepartment
                    .computeIfAbsent(
                            summary.getLeadDepartmentId(),
                            ignored -> new DepartmentFinancialAccumulator(
                                    summary.getLeadDepartmentId(),
                                    summary.getLeadDepartmentName()
                            )
                    )
                    .add(summary);
        }

        return totalsByDepartment.values().stream()
                .filter(this::hasVisibleAmounts)
                .sorted(Comparator.comparingLong(DepartmentFinancialAccumulator::profit).reversed())
                .limit(limit)
                .map(this::toDepartmentFinancialItem)
                .toList();
    }

    @Override
    public DashboardProjectOverviewResponse getProjectOverview(int year) {
        LocalDate startDate = LocalDate.of(year, Month.JANUARY, 1);
        LocalDate endDate = LocalDate.of(year, Month.DECEMBER, 31);
        kr.co.abacus.abms.application.project.dto.ProjectOverviewSummary summary = projectRepository.summarize(
                new kr.co.abacus.abms.application.project.dto.ProjectSearchCondition(
                        null,
                        null,
                        null,
                        startDate,
                        endDate
                )
        );

        return new DashboardProjectOverviewResponse(
                summary.totalCount(),
                summary.scheduledCount(),
                summary.inProgressCount(),
                summary.completedCount(),
                summary.onHoldCount(),
                summary.cancelledCount()
        );
    }

    @Override
    public DashboardEmployeeOverviewResponse getEmployeeOverview(int year) {
        LocalDate endOfYear = LocalDate.of(year, Month.DECEMBER, 31);

        return new DashboardEmployeeOverviewResponse(
                employeeRepository.countEmployedAsOf(endOfYear),
                employeeRepository.countEmployedAsOfByType(endOfYear, EmployeeType.FULL_TIME),
                employeeRepository.countEmployedAsOfByType(endOfYear, EmployeeType.FREELANCER),
                employeeRepository.countEmployedAsOfByType(endOfYear, EmployeeType.OUTSOURCING),
                employeeRepository.countEmployedAsOfByType(endOfYear, EmployeeType.PART_TIME)
        );
    }

    private DashboardUpcomingDeadlineItem toUpcomingDeadlineItem(Project project, LocalDate today) {
        LocalDate endDate = getProjectEndDate(project);
        long daysLeft = ChronoUnit.DAYS.between(today, endDate);

        return new DashboardUpcomingDeadlineItem(
                project.getIdOrThrow(),
                project.getName(),
                partyQueryService.getPartyName(project.getPartyId()),
                project.getStatus().name(),
                project.getStatus().getDescription(),
                endDate,
                daysLeft
        );
    }

    private boolean hasUpcomingDeadline(Project project, LocalDate deadlineBoundary) {
        LocalDate endDate = project.getPeriod().endDate();
        return endDate != null && !endDate.isAfter(deadlineBoundary);
    }

    private LocalDate getProjectEndDate(Project project) {
        return Objects.requireNonNull(project.getPeriod().endDate());
    }

    private List<MonthlyRevenueSummary> getLatestProjectSnapshotsByMonth(int year) {
        LocalDate startDate = LocalDate.of(year, Month.JANUARY, 1);
        LocalDate endDate = LocalDate.of(year, Month.DECEMBER, 31);
        List<MonthlyRevenueSummary> summaries = monthlyRevenueSummaryRepository
                .findAllBySummaryDateBetweenAndDeletedFalseOrderBySummaryDateAscIdAsc(startDate, endDate);

        Map<ProjectMonthKey, MonthlyRevenueSummary> latestProjectSnapshots = new LinkedHashMap<>();
        for (MonthlyRevenueSummary summary : summaries) {
            latestProjectSnapshots.put(
                    new ProjectMonthKey(YearMonth.from(summary.getSummaryDate()), summary.getProjectId()),
                    summary
            );
        }

        return latestProjectSnapshots.values().stream().toList();
    }

    private boolean hasVisibleAmounts(DepartmentFinancialAccumulator accumulator) {
        return accumulator.revenue() != 0L || accumulator.profit() != 0L;
    }

    private DashboardDepartmentFinancialItem toDepartmentFinancialItem(DepartmentFinancialAccumulator accumulator) {
        long revenue = accumulator.revenue();
        long profit = accumulator.profit();

        return new DashboardDepartmentFinancialItem(
                accumulator.departmentId(),
                accumulator.departmentName(),
                revenue,
                profit,
                calculateProfitMargin(profit, revenue)
        );
    }

    private double calculateProfitMargin(long profit, long revenue) {
        if (revenue == 0L) {
            return 0.0D;
        }

        return BigDecimal.valueOf(profit)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(revenue), 1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static final class DepartmentFinancialAccumulator {

        private final Long departmentId;
        private final String departmentName;
        private Money revenue = Money.zero();
        private Money profit = Money.zero();

        private DepartmentFinancialAccumulator(Long departmentId, String departmentName) {
            this.departmentId = departmentId;
            this.departmentName = departmentName;
        }

        private void add(MonthlyRevenueSummary summary) {
            revenue = revenue.add(summary.getRevenueAmount());
            profit = profit.add(summary.getProfitAmount());
        }

        private Long departmentId() {
            return departmentId;
        }

        private String departmentName() {
            return departmentName;
        }

        private long revenue() {
            return revenue.amount().longValue();
        }

        private long profit() {
            return profit.amount().longValue();
        }
    }

    private record ProjectMonthKey(YearMonth month, Long projectId) {
    }

}
