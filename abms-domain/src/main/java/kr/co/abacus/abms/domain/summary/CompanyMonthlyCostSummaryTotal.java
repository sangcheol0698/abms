package kr.co.abacus.abms.domain.summary;

import java.time.LocalDate;

import kr.co.abacus.abms.domain.shared.Money;

public record CompanyMonthlyCostSummaryTotal(
        LocalDate targetMonth,
        Money revenueAmount,
        Money projectAllocatedCostAmount,
        Money unallocatedEmployeeCostAmount,
        Money costAmount,
        Money profitAmount
) {
}
