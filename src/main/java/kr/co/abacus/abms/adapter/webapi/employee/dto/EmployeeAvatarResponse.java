package kr.co.abacus.abms.adapter.webapi.employee.dto;

import kr.co.abacus.abms.domain.employee.EmployeeAvatar;

public record EmployeeAvatarResponse(
    String code,
    String displayName
) {

    public static EmployeeAvatarResponse of(EmployeeAvatar avatar) {
        return new EmployeeAvatarResponse(avatar.name(), avatar.getDisplayName());
    }

}
