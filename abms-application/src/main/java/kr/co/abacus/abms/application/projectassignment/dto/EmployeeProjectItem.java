package kr.co.abacus.abms.application.projectassignment.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

public record EmployeeProjectItem(
        Long projectId,
        String projectCode,
        String projectName,
        Long partyId,
        @Nullable String role,
        LocalDate assignmentStartDate,
        @Nullable LocalDate assignmentEndDate,
        String assignmentStatus,
        String projectStatus,
        String projectStatusDescription,
        @Nullable Long leadDepartmentId,
        @Nullable String leadDepartmentName,
        String partyName
) {

}
