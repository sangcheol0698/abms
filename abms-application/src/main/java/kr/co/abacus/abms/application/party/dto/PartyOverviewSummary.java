package kr.co.abacus.abms.application.party.dto;

public record PartyOverviewSummary(
        long totalCount,
        long withProjectsCount,
        long withInProgressProjectsCount,
        long withoutProjectsCount,
        long totalContractAmount) {

    public static PartyOverviewSummary empty() {
        return new PartyOverviewSummary(0, 0, 0, 0, 0);
    }

}
