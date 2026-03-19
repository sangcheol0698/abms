package kr.co.abacus.abms.application.projectassignment.dto;

import java.util.List;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.project.ProjectStatus;

public record EmployeeProjectSearchCondition(
        Long employeeId,
        @Nullable String name,
        @Nullable List<ProjectAssignmentStatus> assignmentStatuses,
        @Nullable List<ProjectStatus> projectStatuses,
        @Nullable List<Long> accessibleProjectIds,
        @Nullable List<Long> accessibleLeadDepartmentIds
) {

    public EmployeeProjectSearchCondition(
            Long employeeId,
            @Nullable String name,
            @Nullable List<ProjectAssignmentStatus> assignmentStatuses,
            @Nullable List<ProjectStatus> projectStatuses
    ) {
        this(employeeId, name, assignmentStatuses, projectStatuses, null, null);
    }

    public EmployeeProjectSearchCondition withAccess(
            @Nullable List<Long> accessibleProjectIds,
            @Nullable List<Long> accessibleLeadDepartmentIds
    ) {
        return new EmployeeProjectSearchCondition(
                employeeId,
                name,
                assignmentStatuses,
                projectStatuses,
                accessibleProjectIds,
                accessibleLeadDepartmentIds
        );
    }
}
