package kr.co.abacus.abms.adapter.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import kr.co.abacus.abms.application.auth.dto.RegistrationRequestCommand;

public record RegistrationRequest(
        @NotBlank
        @Email
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@iabacus\\.co\\.kr$", message = "회사 이메일(@iabacus.co.kr)만 사용할 수 있습니다.")
        String email
) {

    public RegistrationRequestCommand toCommand() {
        return new RegistrationRequestCommand(email);
    }

}
