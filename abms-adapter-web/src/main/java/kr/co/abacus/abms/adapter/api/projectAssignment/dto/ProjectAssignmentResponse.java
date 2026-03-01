package kr.co.abacus.abms.adapter.api.projectAssignment.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.projectassignment.AssignmentRole;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;

public record ProjectAssignmentResponse(
    Long id,
    Long projectId,
    Long employeeId,
    @Nullable AssignmentRole role,
    LocalDate startDate,
    @Nullable LocalDate endDate
) {
    public static ProjectAssignmentResponse from(ProjectAssignment assignment) {
        return new ProjectAssignmentResponse(
            assignment.getIdOrThrow(),
            assignment.getProjectId(),
            assignment.getEmployeeId(),
            assignment.getRole(),
            assignment.getPeriod().startDate(),
            assignment.getPeriod().endDate()
        );
    }
}
