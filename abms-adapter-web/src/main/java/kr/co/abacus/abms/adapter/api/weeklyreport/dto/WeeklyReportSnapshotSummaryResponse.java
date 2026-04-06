package kr.co.abacus.abms.adapter.api.weeklyreport.dto;

import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportSnapshotSummary;

public record WeeklyReportSnapshotSummaryResponse(
        long totalEmployees,
        long onLeaveEmployees,
        long inProgressProjects,
        long startedProjects,
        long endedProjects,
        boolean monthlyRevenueAvailable) {

    public static WeeklyReportSnapshotSummaryResponse from(WeeklyReportSnapshotSummary summary) {
        return new WeeklyReportSnapshotSummaryResponse(
                summary.totalEmployees(),
                summary.onLeaveEmployees(),
                summary.inProgressProjects(),
                summary.startedProjects(),
                summary.endedProjects(),
                summary.monthlyRevenueAvailable()
        );
    }
}
