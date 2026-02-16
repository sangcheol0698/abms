package kr.co.abacus.abms.adapter.api.summary.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;

public record MonthlyRevenueSummaryResponse(
    LocalDate targetMonth,
    BigDecimal revenue,
    BigDecimal cost,
    BigDecimal profit
) {
    public static MonthlyRevenueSummaryResponse of(MonthlyRevenueSummary summary) {
        return new MonthlyRevenueSummaryResponse(
            summary.getSummaryDate(),
            summary.getRevenueAmount().amount(),
            summary.getCostAmount().amount(),
            summary.getProfitAmount().amount()
        );
    }
}
