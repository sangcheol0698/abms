package kr.co.abacus.abms.adapter.api.admin.dto;

import java.util.List;

public record PermissionGroupDetailResponse(
        Long id,
        String name,
        String description,
        String groupType,
        List<PermissionGroupGrantResponse> grants,
        List<PermissionGroupAccountResponse> accounts
) {
}
