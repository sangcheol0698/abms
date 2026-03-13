package kr.co.abacus.abms.adapter.api.admin.dto;

import java.util.List;

import kr.co.abacus.abms.adapter.api.common.EnumResponse;
import kr.co.abacus.abms.application.permission.dto.PermissionCatalogItem;

public record PermissionCatalogResponse(
        String code,
        String name,
        String description,
        List<EnumResponse> availableScopes
) {

    public static PermissionCatalogResponse from(PermissionCatalogItem item) {
        return new PermissionCatalogResponse(
                item.code(),
                item.name(),
                item.description(),
                item.availableScopes().stream()
                        .map(scope -> new EnumResponse(scope.name(), scope.getDescription(), scope.getOrder()))
                        .toList());
    }
}
