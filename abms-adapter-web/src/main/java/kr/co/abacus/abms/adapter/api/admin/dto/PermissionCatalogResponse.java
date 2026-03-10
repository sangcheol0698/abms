package kr.co.abacus.abms.adapter.api.admin.dto;

import kr.co.abacus.abms.application.permission.dto.PermissionCatalogItem;

public record PermissionCatalogResponse(
        String code,
        String name,
        String description
) {

    public static PermissionCatalogResponse from(PermissionCatalogItem item) {
        return new PermissionCatalogResponse(item.code(), item.name(), item.description());
    }
}
