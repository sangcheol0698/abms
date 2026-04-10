package kr.co.abacus.abms.application.dashboard.dto;

public record DashboardProjectOverviewResponse(
        long totalCount,
        long scheduledCount,
        long inProgressCount,
        long completedCount,
        long onHoldCount,
        long cancelledCount
) {
}
