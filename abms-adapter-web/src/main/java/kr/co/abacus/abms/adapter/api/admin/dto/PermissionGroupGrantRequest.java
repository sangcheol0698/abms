package kr.co.abacus.abms.adapter.api.admin.dto;

import java.util.List;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import kr.co.abacus.abms.application.permission.dto.PermissionGroupGrantCommand;
import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;

public record PermissionGroupGrantRequest(
        @NotBlank String permissionCode,
        @NotEmpty List<PermissionScope> scopes
) {

    public PermissionGroupGrantCommand toCommand() {
        return new PermissionGroupGrantCommand(permissionCode, Set.copyOf(scopes));
    }
}
