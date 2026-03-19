package kr.co.abacus.abms.application.projectassignment.dto;

import java.util.List;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.projectassignment.AssignmentRole;

public record ProjectAssignmentSearchCondition(
        Long projectId,
        @Nullable String name,
        @Nullable List<ProjectAssignmentStatus> assignmentStatuses,
        @Nullable List<AssignmentRole> roles
) {

}
