package kr.co.abacus.abms.adapter.api.admin.dto;

import jakarta.validation.constraints.NotNull;

public record PermissionGroupAccountAssignRequest(
        @NotNull Long accountId
) {
}
