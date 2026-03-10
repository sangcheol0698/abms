package kr.co.abacus.abms.adapter.api.admin.dto;

import kr.co.abacus.abms.adapter.api.common.EnumResponse;
import kr.co.abacus.abms.application.permission.dto.PermissionGroupAccountSummary;

public record PermissionGroupAccountResponse(
        Long accountId,
        Long employeeId,
        String employeeName,
        String email,
        String departmentName,
        EnumResponse position
) {

    public static PermissionGroupAccountResponse from(PermissionGroupAccountSummary summary) {
        return new PermissionGroupAccountResponse(
                summary.accountId(),
                summary.employeeId(),
                summary.employeeName(),
                summary.email(),
                summary.departmentName(),
                new EnumResponse(
                        summary.position().name(),
                        summary.position().getDescription(),
                        summary.position().getLevel()));
    }
}
