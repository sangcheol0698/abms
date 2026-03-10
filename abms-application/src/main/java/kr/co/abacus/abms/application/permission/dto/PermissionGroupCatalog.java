package kr.co.abacus.abms.application.permission.dto;

import java.util.List;

import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;

public record PermissionGroupCatalog(
        List<PermissionCatalogItem> permissions,
        List<PermissionScope> scopes
) {
}
