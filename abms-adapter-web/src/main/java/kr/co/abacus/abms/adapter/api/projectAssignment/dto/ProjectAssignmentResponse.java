package kr.co.abacus.abms.adapter.api.projectAssignment.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.employee.dto.EmployeeDetail;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;

public record ProjectAssignmentResponse(
    Long id,
    Long projectId,
    Long employeeId,
    @Nullable String employeeName,
    @Nullable Long departmentId,
    @Nullable String departmentName,
    @Nullable String role,
    LocalDate startDate,
    @Nullable LocalDate endDate,
    String assignmentStatus
) {
    public static ProjectAssignmentResponse from(
            ProjectAssignment assignment,
            @Nullable EmployeeDetail employeeDetail,
            String assignmentStatus
    ) {
        return new ProjectAssignmentResponse(
            assignment.getIdOrThrow(),
            assignment.getProjectId(),
            assignment.getEmployeeId(),
            employeeDetail != null ? employeeDetail.name() : null,
            employeeDetail != null ? employeeDetail.departmentId() : null,
            employeeDetail != null ? employeeDetail.departmentName() : null,
            assignment.getRole() != null ? assignment.getRole().name() : null,
            assignment.getPeriod().startDate(),
            assignment.getPeriod().endDate(),
            assignmentStatus
        );
    }
}
