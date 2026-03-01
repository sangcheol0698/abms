package kr.co.abacus.abms.adapter.api.auth.dto;

import kr.co.abacus.abms.application.auth.dto.AuthenticatedUserInfo;

public record AuthMeResponse(
        String name,
        String email
) {

    public static AuthMeResponse from(AuthenticatedUserInfo userInfo) {
        return new AuthMeResponse(userInfo.name(), userInfo.email());
    }

}
