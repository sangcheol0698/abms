package kr.co.abacus.abms.application.auth.dto;

public record PasswordResetConfirmCommand(
        String token,
        String password
) {

}
