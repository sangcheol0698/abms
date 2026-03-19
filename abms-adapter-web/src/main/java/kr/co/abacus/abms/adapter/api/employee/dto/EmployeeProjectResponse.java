package kr.co.abacus.abms.adapter.api.employee.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.projectassignment.dto.EmployeeProjectItem;

public record EmployeeProjectResponse(
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

    public static EmployeeProjectResponse from(EmployeeProjectItem item) {
        return new EmployeeProjectResponse(
                item.projectId(),
                item.projectCode(),
                item.projectName(),
                item.partyId(),
                item.role(),
                item.assignmentStartDate(),
                item.assignmentEndDate(),
                item.assignmentStatus(),
                item.projectStatus(),
                item.projectStatusDescription(),
                item.leadDepartmentId(),
                item.leadDepartmentName(),
                item.partyName()
        );
    }

}
