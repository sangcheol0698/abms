package kr.co.abacus.abms.application.dashboard.dto;

public record DashboardSummaryResponse(
    int totalEmployeesCount,
    int activeProjectsCount,
    int newEmployeesCount,
    int onLeaveEmployeesCount
    ) {

}
