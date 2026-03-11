package kr.co.abacus.abms.application.project.dto;

public record ProjectOverviewSummary(
        long totalCount,
        long scheduledCount,
        long inProgressCount,
        long completedCount,
        long onHoldCount,
        long cancelledCount,
        long totalContractAmount) {

    public static ProjectOverviewSummary empty() {
        return new ProjectOverviewSummary(0, 0, 0, 0, 0, 0, 0);
    }

}
