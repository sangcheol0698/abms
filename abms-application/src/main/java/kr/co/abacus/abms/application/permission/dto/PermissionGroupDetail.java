package kr.co.abacus.abms.application.permission.dto;

import java.util.List;

import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType;

public record PermissionGroupDetail(
        Long id,
        String name,
        String description,
        PermissionGroupType groupType,
        List<PermissionGroupGrantDetail> grants,
        List<PermissionGroupAccountSummary> accounts
) {
}
