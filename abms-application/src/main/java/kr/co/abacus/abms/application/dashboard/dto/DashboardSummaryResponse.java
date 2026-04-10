package kr.co.abacus.abms.application.dashboard.dto;

public record DashboardSummaryResponse(
        int totalEmployeesCount,
        int activeProjectsCount,
        int completedProjectsCount,
        int newEmployeesCount,
        long yearRevenue,
        long yearProfit
) {

}
