package kr.co.abacus.abms.application.employee.dto;

public record EmployeeOverviewSummary(
        long totalCount,
        long activeCount,
        long onLeaveCount,
        long fullTimeCount,
        long freelancerCount,
        long outsourcingCount,
        long partTimeCount) {

    public static EmployeeOverviewSummary empty() {
        return new EmployeeOverviewSummary(0, 0, 0, 0, 0, 0, 0);
    }

}
