package kr.co.abacus.abms.adapter.api.auth.dto;

import java.util.List;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.auth.dto.AuthenticatedUserInfo;

public record AuthMeResponse(
        String name,
        String email,
        @Nullable Long employeeId,
        @Nullable Long departmentId,
        List<AuthPermissionResponse> permissions
) {

    public static AuthMeResponse from(AuthenticatedUserInfo userInfo) {
        return new AuthMeResponse(
                userInfo.name(),
                userInfo.email(),
                userInfo.employeeId(),
                userInfo.departmentId(),
                userInfo.permissions().stream()
                        .map(AuthPermissionResponse::from)
                        .toList()
        );
    }

}
