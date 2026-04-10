package kr.co.abacus.abms.application.dashboard.dto;

import java.time.LocalDate;

public record DashboardMonthlyFinancialItem(
        LocalDate targetMonth,
        long revenue,
        long cost,
        long profit
) {
}
