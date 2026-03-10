package kr.co.abacus.abms.adapter.api.admin.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import kr.co.abacus.abms.application.permission.dto.PermissionGroupUpsertCommand;

public record PermissionGroupUpsertRequest(
        @NotBlank @Size(max = 50) String name,
        @NotBlank @Size(max = 255) String description,
        @NotNull @Valid List<PermissionGroupGrantRequest> grants
) {

    public PermissionGroupUpsertCommand toCommand() {
        return new PermissionGroupUpsertCommand(
                name,
                description,
                grants.stream().map(PermissionGroupGrantRequest::toCommand).toList());
    }
}
