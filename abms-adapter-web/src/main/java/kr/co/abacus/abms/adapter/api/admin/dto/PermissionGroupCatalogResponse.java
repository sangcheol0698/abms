package kr.co.abacus.abms.adapter.api.admin.dto;

import java.util.List;

import kr.co.abacus.abms.adapter.api.common.EnumResponse;

public record PermissionGroupCatalogResponse(
        List<PermissionCatalogResponse> permissions,
        List<EnumResponse> scopes
) {
}
