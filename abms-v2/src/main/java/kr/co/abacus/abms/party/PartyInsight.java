package kr.co.abacus.abms.party;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.project.ProjectRevenuePlan;
import kr.co.abacus.abms.project.ProjectStatus;

/**
 * 협력사 상세 화면의 거래 요약. 조회 가능한 프로젝트와 그 매출(청구) 계획에서 계산한다.
 *
 * @param projectCount     프로젝트 수
 * @param inProgressCount  진행 중 프로젝트 수
 * @param totalContract    누적 계약금액
 * @param issuedTotal      발행(청구 완료) 매출 누적
 * @param unissuedPlanned  계획됐지만 아직 발행하지 않은 매출
 * @param unplanned        계약금액 중 아직 청구 계획이 없는 금액
 * @param thisYearIssued   올해 발행 매출
 * @param yearly           최근 5년 연도별 발행 매출
 * @param upcoming         기한이 지났거나 90일 안에 청구 예정인 미발행 계획
 * @param firstStartDate   첫 거래(가장 이른 프로젝트 시작일)
 */
public record PartyInsight(
        int projectCount,
        int inProgressCount,
        Money totalContract,
        Money issuedTotal,
        Money unissuedPlanned,
        Money unplanned,
        Money thisYearIssued,
        List<YearAmount> yearly,
        List<UpcomingPlan> upcoming,
        @Nullable LocalDate firstStartDate
) {

    public static PartyInsight of(List<Project> projects, List<ProjectRevenuePlan> plans, LocalDate today) {
        Map<Long, Project> byId = projects.stream().collect(Collectors.toMap(Project::id, Function.identity()));
        List<ProjectRevenuePlan> visible = plans.stream().filter(p -> byId.containsKey(p.getProjectId())).toList();
        Money contract = projects.stream().map(Project::getContractAmount).reduce(Money.ZERO, Money::plus);
        Money issued = sum(visible.stream().filter(ProjectRevenuePlan::isIssued).toList());
        Money planned = sum(visible);
        Money thisYear = sum(visible.stream().filter(p -> p.isIssued() && p.getRevenueDate().getYear() == today.getYear()).toList());

        List<YearAmount> yearly = new ArrayList<>();
        for (int year = today.getYear() - 4; year <= today.getYear(); year++) {
            int y = year;
            yearly.add(new YearAmount(y, sum(visible.stream().filter(p -> p.isIssued() && p.getRevenueDate().getYear() == y).toList())));
        }
        List<UpcomingPlan> upcoming = visible.stream()
                .filter(p -> !p.isIssued() && !p.getRevenueDate().isAfter(today.plusDays(90)))
                .sorted(Comparator.comparing(ProjectRevenuePlan::getRevenueDate))
                .map(p -> new UpcomingPlan(byId.get(p.getProjectId()), p, p.getRevenueDate().isBefore(today)))
                .toList();
        Money unplanned = contract.minus(planned);
        return new PartyInsight(projects.size(),
                (int) projects.stream().filter(p -> p.getStatus() == ProjectStatus.IN_PROGRESS).count(),
                contract, issued, planned.minus(issued), unplanned.isNegative() ? Money.ZERO : unplanned, thisYear,
                List.copyOf(yearly), upcoming,
                projects.stream().map(p -> p.getPeriod().startDate()).min(Comparator.naturalOrder()).orElse(null));
    }

    private static Money sum(List<ProjectRevenuePlan> plans) {
        return plans.stream().map(ProjectRevenuePlan::getAmount).reduce(Money.ZERO, Money::plus);
    }

    /** 연도별 막대 높이 비율 (가장 큰 해 = 100) */
    public int yearPercent(YearAmount item) {
        java.math.BigDecimal max = yearly.stream().map(y -> y.amount().amount()).max(Comparator.naturalOrder()).orElse(java.math.BigDecimal.ZERO);
        if (max.signum() <= 0) {
            return 0;
        }
        return item.amount().amount().multiply(java.math.BigDecimal.valueOf(100)).divide(max, 0, java.math.RoundingMode.HALF_UP).intValue();
    }

    public record YearAmount(int year, Money amount) {
    }

    public record UpcomingPlan(Project project, ProjectRevenuePlan plan, boolean overdue) {
    }

}
