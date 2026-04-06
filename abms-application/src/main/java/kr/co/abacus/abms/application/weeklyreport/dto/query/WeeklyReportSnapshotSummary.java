package kr.co.abacus.abms.application.weeklyreport.dto.query;

public record WeeklyReportSnapshotSummary(
        long totalEmployees,
        long onLeaveEmployees,
        long inProgressProjects,
        long startedProjects,
        long endedProjects,
        boolean monthlyRevenueAvailable) {

}
