package kr.co.abacus.abms.application.projectassignment.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

public record ProjectAssignmentItem(
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

}
