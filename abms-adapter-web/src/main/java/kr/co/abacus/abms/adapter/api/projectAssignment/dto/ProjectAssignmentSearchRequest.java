package kr.co.abacus.abms.adapter.api.projectAssignment.dto;

import java.util.List;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.projectassignment.dto.ProjectAssignmentSearchCondition;
import kr.co.abacus.abms.application.projectassignment.dto.ProjectAssignmentStatus;
import kr.co.abacus.abms.domain.projectassignment.AssignmentRole;

public record ProjectAssignmentSearchRequest(
        @Nullable String name,
        @Nullable List<ProjectAssignmentStatus> assignmentStatuses,
        @Nullable List<AssignmentRole> roles
) {

    public ProjectAssignmentSearchCondition toCondition(Long projectId) {
        return new ProjectAssignmentSearchCondition(projectId, name, assignmentStatuses, roles);
    }
}
