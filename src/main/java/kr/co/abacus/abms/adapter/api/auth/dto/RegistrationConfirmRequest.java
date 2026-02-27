package kr.co.abacus.abms.adapter.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

import kr.co.abacus.abms.application.auth.dto.RegistrationConfirmCommand;

public record RegistrationConfirmRequest(
        @NotBlank String token,
        @NotBlank String password
) {

    public RegistrationConfirmCommand toCommand() {
        return new RegistrationConfirmCommand(token, password);
    }

}
