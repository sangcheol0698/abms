package kr.co.abacus.abms.adapter.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import kr.co.abacus.abms.application.auth.dto.LoginCommand;

public record LoginRequest(
        @NotBlank @Email String username,
        @NotBlank String password
) {

    public LoginCommand toCommand() {
        return new LoginCommand(username, password);
    }

}
