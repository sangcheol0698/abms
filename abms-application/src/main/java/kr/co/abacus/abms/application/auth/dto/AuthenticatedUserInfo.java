package kr.co.abacus.abms.application.auth.dto;

import java.util.List;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.permission.dto.GrantedPermissionDetail;

public record AuthenticatedUserInfo(
        String name,
        String email,
        @Nullable Long employeeId,
        @Nullable Long departmentId,
        List<GrantedPermissionDetail> permissions
) {

}
