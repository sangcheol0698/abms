package kr.co.abacus.abms.adapter.api.projectAssignment.dto;

import java.time.LocalDate;

import kr.co.abacus.abms.domain.projectassignment.AssignmentRole;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;

public record ProjectAssignmentResponse(
    Long id,
    Long projectId,
    Long employeeId,
    AssignmentRole role,
    LocalDate startDate,
    LocalDate endDate
) {
    public static ProjectAssignmentResponse from(ProjectAssignment assignment) {
        return new ProjectAssignmentResponse(
            assignment.getId(),
            assignment.getProjectId(),
            assignment.getEmployeeId(),
            assignment.getRole(),
            assignment.getPeriod().startDate(),
            assignment.getPeriod().endDate()
        );
    }
}