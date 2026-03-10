package kr.co.abacus.abms.application.permission.dto;

import java.util.List;

public record PermissionGroupUpsertCommand(
        String name,
        String description,
        List<PermissionGroupGrantCommand> grants
) {
}
