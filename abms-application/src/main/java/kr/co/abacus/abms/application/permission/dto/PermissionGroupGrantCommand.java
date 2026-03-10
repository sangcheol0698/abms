package kr.co.abacus.abms.application.permission.dto;

import java.util.Set;

import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;

public record PermissionGroupGrantCommand(
        String permissionCode,
        Set<PermissionScope> scopes
) {
}
