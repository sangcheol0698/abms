package kr.co.abacus.abms.adapter.api.summary.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import kr.co.abacus.abms.domain.summary.CompanyMonthlyCostSummaryTotal;

public record CompanyMonthlyCostSummaryResponse(
        LocalDate targetMonth,
        BigDecimal revenue,
        BigDecimal projectAllocatedCost,
        BigDecimal unallocatedEmployeeCost,
        BigDecimal cost,
        BigDecimal profit
) {
    public static CompanyMonthlyCostSummaryResponse of(CompanyMonthlyCostSummaryTotal summary) {
        return new CompanyMonthlyCostSummaryResponse(
                summary.targetMonth(),
                summary.revenueAmount().amount(),
                summary.projectAllocatedCostAmount().amount(),
                summary.unallocatedEmployeeCostAmount().amount(),
                summary.costAmount().amount(),
                summary.profitAmount().amount()
        );
    }
}
