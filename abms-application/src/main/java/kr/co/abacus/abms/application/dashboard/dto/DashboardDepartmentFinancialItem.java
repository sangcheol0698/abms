package kr.co.abacus.abms.application.dashboard.dto;

public record DashboardDepartmentFinancialItem(
        Long departmentId,
        String departmentName,
        long revenue,
        long profit,
        double profitMargin
) {
}
