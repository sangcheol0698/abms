package kr.co.abacus.abms.summary;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.domain.Money;

/**
 * 월 손익 리포트.
 *
 * @param companyCost 전사 비용 집계 (전체 범위 사용자에게만 제공)
 */
public record MonthReport(
        YearMonth month,
        boolean closed,
        @Nullable LocalDateTime calculatedAt,
        List<ProfitRow> projects,
        List<ProfitRow> departments,
        @Nullable CompanyMonthlyCostSummary companyCost
) {

    public Money revenue() {
        return projects.stream().map(ProfitRow::revenue).reduce(Money.ZERO, Money::plus);
    }

    public Money cost() {
        return projects.stream().map(ProfitRow::cost).reduce(Money.ZERO, Money::plus);
    }

    public Money profit() {
        return revenue().minus(cost());
    }

    public BigDecimal margin() {
        return ProfitRow.margin(revenue(), cost());
    }

    public boolean calculated() {
        return calculatedAt != null;
    }

}
