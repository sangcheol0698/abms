package kr.co.abacus.abms.application.auth.dto;

import java.util.List;

import kr.co.abacus.abms.application.permission.dto.GrantedPermissionDetail;

public record AuthenticatedUserInfo(
        String name,
        String email,
        List<GrantedPermissionDetail> permissions
) {

}
