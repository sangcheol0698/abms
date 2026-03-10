package kr.co.abacus.abms.adapter.api.admin.dto;

import java.util.List;

import kr.co.abacus.abms.application.permission.dto.PermissionGroupGrantDetail;

public record PermissionGroupGrantResponse(
        String permissionCode,
        String permissionName,
        String permissionDescription,
        List<String> scopes
) {

    public static PermissionGroupGrantResponse from(PermissionGroupGrantDetail detail) {
        return new PermissionGroupGrantResponse(
                detail.permissionCode(),
                detail.permissionName(),
                detail.permissionDescription(),
                detail.scopes().stream().map(Enum::name).toList());
    }
}
