package kr.co.abacus.abms.domain.summary;

import java.time.LocalDate;

import kr.co.abacus.abms.domain.shared.Money;

public record MonthlyRevenueSummaryTotal(
        LocalDate targetMonth,
        Money revenueAmount,
        Money costAmount,
        Money profitAmount
) {
}
