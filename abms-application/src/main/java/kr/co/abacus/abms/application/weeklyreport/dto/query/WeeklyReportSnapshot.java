package kr.co.abacus.abms.application.weeklyreport.dto.query;

import java.time.LocalDate;
import java.util.List;

import org.jspecify.annotations.Nullable;

public record WeeklyReportSnapshot(
        LocalDate weekStart,
        LocalDate weekEnd,
        EmployeeSection employees,
        ProjectSection projects,
        RevenueSection revenue,
        RiskSection risks) {

    public record EmployeeSection(
            long totalEmployees,
            long activeEmployees,
            long onLeaveEmployees,
            long resignedEmployees,
            long departmentCount,
            long joinedThisWeek,
            long resignedThisWeek,
            boolean statusChangeTrackingLimited,
            List<DepartmentOnLeave> departmentsWithOnLeaveSignals) {

    }

    public record DepartmentOnLeave(
            Long departmentId,
            String departmentName,
            long onLeaveEmployees) {

    }

    public record ProjectSection(
            long inProgressProjects,
            long completedProjects,
            long cancelledProjects,
            long startedThisWeek,
            long endedThisWeek,
            long startedContractAmount,
            long endedContractAmount,
            long assignmentChangeProjectCount,
            List<ProjectDeadlineItem> endingSoonProjects,
            List<ProjectCoverageItem> projectsWithoutAssignments) {

    }

    public record ProjectDeadlineItem(
            Long projectId,
            String projectName,
            LocalDate endDate) {

    }

    public record ProjectCoverageItem(
            Long projectId,
            String projectName) {

    }

    public record RevenueSection(
            boolean monthlySummaryAvailable,
            String targetMonth,
            @Nullable Long revenueAmount,
            @Nullable Long costAmount,
            @Nullable Long profitAmount) {

    }

    public record RiskSection(
            List<String> endingSoonProjectNames,
            List<String> noAssignmentProjectNames,
            List<String> onLeaveDepartmentNames) {

    }
}
