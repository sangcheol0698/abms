package kr.co.abacus.abms.application.permission.dto;

import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType;

public record PermissionGroupSummary(
        Long id,
        String name,
        String description,
        PermissionGroupType groupType,
        int assignedAccountCount,
        int grantCount
) {
}
