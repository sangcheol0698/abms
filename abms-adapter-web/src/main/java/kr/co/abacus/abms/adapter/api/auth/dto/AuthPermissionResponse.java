package kr.co.abacus.abms.adapter.api.auth.dto;

import java.util.List;

import kr.co.abacus.abms.application.permission.dto.GrantedPermissionDetail;
import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;

public record AuthPermissionResponse(
        String code,
        List<PermissionScope> scopes
) {

    public static AuthPermissionResponse from(GrantedPermissionDetail permissionDetail) {
        return new AuthPermissionResponse(
                permissionDetail.code(),
                List.copyOf(permissionDetail.scopes())
        );
    }

}
