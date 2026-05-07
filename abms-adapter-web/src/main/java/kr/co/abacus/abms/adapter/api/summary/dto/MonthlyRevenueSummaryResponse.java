package kr.co.abacus.abms.adapter.api.summary.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummaryTotal;

public record MonthlyRevenueSummaryResponse(
    LocalDate targetMonth,
    BigDecimal revenue,
    BigDecimal cost,
    BigDecimal profit
) {
    public static MonthlyRevenueSummaryResponse of(MonthlyRevenueSummaryTotal summary) {
        return new MonthlyRevenueSummaryResponse(
            summary.targetMonth(),
            summary.revenueAmount().amount(),
            summary.costAmount().amount(),
            summary.profitAmount().amount()
        );
    }
}
