package kr.co.abacus.abms.adapter.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import kr.co.abacus.abms.application.auth.dto.RegistrationConfirmCommand;

public record RegistrationConfirmRequest(
        @NotBlank String token,
        @NotBlank
        @Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하여야 합니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
                message = "비밀번호는 영문, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다."
        )
        String password
) {

    public RegistrationConfirmCommand toCommand() {
        return new RegistrationConfirmCommand(token, password);
    }

}
