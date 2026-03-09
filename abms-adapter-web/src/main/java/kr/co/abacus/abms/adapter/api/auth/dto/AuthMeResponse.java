package kr.co.abacus.abms.adapter.api.auth.dto;

import java.util.List;

import kr.co.abacus.abms.application.auth.dto.AuthenticatedUserInfo;

public record AuthMeResponse(
        String name,
        String email,
        List<AuthPermissionResponse> permissions
) {

    public static AuthMeResponse from(AuthenticatedUserInfo userInfo) {
        return new AuthMeResponse(
                userInfo.name(),
                userInfo.email(),
                userInfo.permissions().stream()
                        .map(AuthPermissionResponse::from)
                        .toList()
        );
    }

}
