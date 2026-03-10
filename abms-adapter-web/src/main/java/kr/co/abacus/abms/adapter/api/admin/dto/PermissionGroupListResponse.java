package kr.co.abacus.abms.adapter.api.admin.dto;

import kr.co.abacus.abms.application.permission.dto.PermissionGroupSummary;

public record PermissionGroupListResponse(
        Long id,
        String name,
        String description,
        String groupType,
        int assignedAccountCount,
        int grantCount
) {

    public static PermissionGroupListResponse from(PermissionGroupSummary summary) {
        return new PermissionGroupListResponse(
                summary.id(),
                summary.name(),
                summary.description(),
                summary.groupType().name(),
                summary.assignedAccountCount(),
                summary.grantCount());
    }
}
