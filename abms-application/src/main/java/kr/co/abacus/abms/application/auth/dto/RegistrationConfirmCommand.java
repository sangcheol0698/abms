package kr.co.abacus.abms.application.auth.dto;

public record RegistrationConfirmCommand(
        String token,
        String password
) {

}
