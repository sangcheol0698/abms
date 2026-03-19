package kr.co.abacus.abms.adapter.api.employee.dto;

import java.util.List;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.projectassignment.dto.EmployeeProjectSearchCondition;
import kr.co.abacus.abms.application.projectassignment.dto.ProjectAssignmentStatus;
import kr.co.abacus.abms.domain.project.ProjectStatus;

public record EmployeeProjectSearchRequest(
        @Nullable String name,
        @Nullable List<ProjectAssignmentStatus> assignmentStatuses,
        @Nullable List<ProjectStatus> projectStatuses
) {

    public EmployeeProjectSearchCondition toCondition(Long employeeId) {
        return new EmployeeProjectSearchCondition(
                employeeId,
                name,
                assignmentStatuses,
                projectStatuses
        );
    }
}
