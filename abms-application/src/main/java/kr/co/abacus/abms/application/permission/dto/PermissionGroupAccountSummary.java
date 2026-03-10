package kr.co.abacus.abms.application.permission.dto;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.employee.EmployeePosition;

public record PermissionGroupAccountSummary(
        Long accountId,
        Long employeeId,
        String employeeName,
        String email,
        @Nullable String departmentName,
        EmployeePosition position
) {
}
