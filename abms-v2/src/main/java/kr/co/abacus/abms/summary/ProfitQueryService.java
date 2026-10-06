package kr.co.abacus.abms.summary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.security.AccessService;
import kr.co.abacus.abms.security.DataScope;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 손익 집계 조회. 대시보드 조회 권한(dashboard.read)의 범위 안에서만 보여준다.
 */
@Service
@Transactional(readOnly = true)
public class ProfitQueryService {

    private final MonthlyRevenueSummaryRepository summaryRepository;
    private final CompanyMonthlyCostSummaryRepository companySummaryRepository;
    private final RevenueMonthClosingRepository closingRepository;
    private final AccessService accessService;

    public ProfitQueryService(MonthlyRevenueSummaryRepository summaryRepository,
                              CompanyMonthlyCostSummaryRepository companySummaryRepository,
                              RevenueMonthClosingRepository closingRepository, AccessService accessService) {
        this.summaryRepository = summaryRepository;
        this.companySummaryRepository = companySummaryRepository;
        this.closingRepository = closingRepository;
        this.accessService = accessService;
    }

    public DataScope scope(LoginUser user) {
        return accessService.scopeOf(user, PermissionCode.DASHBOARD_READ);
    }

    public MonthReport monthReport(LoginUser user, YearMonth month) {
        return monthReport(user, month, RevenueBasis.BILLING);
    }

    public MonthReport monthReport(LoginUser user, YearMonth month, RevenueBasis basis) {
        DataScope scope = scope(user);
        LocalDate monthStart = month.atDay(1);
        List<MonthlyRevenueSummary> summaries = summaryRepository.findAllByTargetMonthOrderByProjectNameAsc(monthStart).stream()
                .filter(s -> scope.coversProject(s.getProjectId(), s.getLeadDepartmentId()))
                .toList();

        List<ProfitRow> projects = summaries.stream()
                .map(s -> new ProfitRow(s.getProjectId(), s.getProjectCode(), s.getProjectName(), basis.revenueOf(s), s.getCostAmount()))
                .sorted(Comparator.comparing(ProfitRow::profit).reversed())
                .toList();
        LocalDateTime calculatedAt = summaries.stream().map(MonthlyRevenueSummary::getCalculatedAt)
                .max(Comparator.naturalOrder()).orElse(null);
        CompanyMonthlyCostSummary companyCost = scope.all() ? companySummaryRepository.findByTargetMonth(monthStart).orElse(null) : null;
        if (calculatedAt == null && companyCost != null) {
            calculatedAt = companyCost.getCalculatedAt();
        }
        boolean closed = closingRepository.existsByTargetMonthAndClosedTrue(monthStart);
        Money directCost = summaries.stream().map(MonthlyRevenueSummary::getDirectCostAmount).reduce(Money.ZERO, Money::plus);
        return new MonthReport(month, basis, closed, calculatedAt, projects, byDepartment(summaries, basis), directCost, companyCost);
    }

    /** 연간 월별 추이 (1~12월) */
    public List<MonthPoint> yearlyTrend(LoginUser user, int year) {
        DataScope scope = scope(user);
        Map<YearMonth, MonthPoint> points = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            YearMonth ym = YearMonth.of(year, m);
            points.put(ym, new MonthPoint(ym, Money.ZERO, Money.ZERO));
        }
        summaryRepository.findAllByTargetMonthBetweenOrderByTargetMonthAsc(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 1))
                .stream()
                .filter(s -> scope.coversProject(s.getProjectId(), s.getLeadDepartmentId()))
                .forEach(s -> points.computeIfPresent(YearMonth.from(s.getTargetMonth()),
                        (k, p) -> new MonthPoint(k, p.revenue().plus(s.getRevenueAmount()), p.cost().plus(s.getCostAmount()))));
        return List.copyOf(points.values());
    }

    /** 연간 프로젝트별 누적 손익 */
    public List<ProfitRow> yearlyProjects(LoginUser user, int year) {
        return aggregate(yearSummaries(user, year), RevenueBasis.BILLING, s -> s.getProjectId(), s -> s.getProjectCode(), s -> s.getProjectName());
    }

    /** 연간 부서별 누적 손익 */
    public List<ProfitRow> yearlyDepartments(LoginUser user, int year) {
        return byDepartment(yearSummaries(user, year), RevenueBasis.BILLING);
    }

    public List<CompanyMonthlyCostSummary> companyCosts(int year) {
        return companySummaryRepository.findAllByTargetMonthBetweenOrderByTargetMonthAsc(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 1));
    }

    public List<MonthlyRevenueSummary> projectHistory(Long projectId) {
        return summaryRepository.findAllByProjectIdOrderByTargetMonthAsc(projectId);
    }

    public List<MonthPoint> departmentTrend(java.util.Collection<Long> departmentIds, int year) {
        Map<YearMonth, MonthPoint> points = new LinkedHashMap<>();
        for (int m = 1; m <= 12; m++) {
            YearMonth ym = YearMonth.of(year, m);
            points.put(ym, new MonthPoint(ym, Money.ZERO, Money.ZERO));
        }
        summaryRepository.findAllByLeadDepartmentIdInAndTargetMonthBetweenOrderByTargetMonthAsc(departmentIds,
                        LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 1))
                .forEach(s -> points.computeIfPresent(YearMonth.from(s.getTargetMonth()),
                        (k, p) -> new MonthPoint(k, p.revenue().plus(s.getRevenueAmount()), p.cost().plus(s.getCostAmount()))));
        return List.copyOf(points.values());
    }

    public List<YearMonth> closedMonths() {
        return closingRepository.findAllByClosedTrueOrderByTargetMonthDesc().stream()
                .map(c -> YearMonth.from(c.getTargetMonth()))
                .toList();
    }

    private List<MonthlyRevenueSummary> yearSummaries(LoginUser user, int year) {
        DataScope scope = scope(user);
        return summaryRepository.findAllByTargetMonthBetweenOrderByTargetMonthAsc(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 1))
                .stream()
                .filter(s -> scope.coversProject(s.getProjectId(), s.getLeadDepartmentId()))
                .toList();
    }

    private static List<ProfitRow> byDepartment(List<MonthlyRevenueSummary> summaries, RevenueBasis basis) {
        return aggregate(summaries, basis, MonthlyRevenueSummary::getLeadDepartmentId, s -> "", MonthlyRevenueSummary::getLeadDepartmentName);
    }

    private static List<ProfitRow> aggregate(List<MonthlyRevenueSummary> summaries, RevenueBasis basis,
                                             java.util.function.Function<MonthlyRevenueSummary, Long> key,
                                             java.util.function.Function<MonthlyRevenueSummary, String> code,
                                             java.util.function.Function<MonthlyRevenueSummary, String> name) {
        Map<Long, ProfitRow> rows = new LinkedHashMap<>();
        for (MonthlyRevenueSummary s : summaries) {
            Long id = key.apply(s);
            ProfitRow row = rows.get(id);
            rows.put(id, Objects.requireNonNullElseGet(row, () -> new ProfitRow(id, code.apply(s), name.apply(s), Money.ZERO, Money.ZERO))
                    .plus(basis.revenueOf(s), s.getCostAmount()));
        }
        List<ProfitRow> result = new ArrayList<>(rows.values());
        result.sort(Comparator.comparing(ProfitRow::profit).reversed());
        return result;
    }

    /** Chart.js 월별 손익 차트 데이터 (app.js renderCharts 형식) */
    public static String chartJson(List<MonthPoint> trend) {
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

    public record MonthPoint(YearMonth month, Money revenue, Money cost) {

        public Money profit() {
            return revenue.minus(cost);
        }

    }

}
