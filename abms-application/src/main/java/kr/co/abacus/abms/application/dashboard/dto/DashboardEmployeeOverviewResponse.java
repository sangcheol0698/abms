package kr.co.abacus.abms.application.dashboard.dto;

public record DashboardEmployeeOverviewResponse(
        long totalCount,
        long fullTimeCount,
        long freelancerCount,
        long outsourcingCount,
        long partTimeCount
) {
}
