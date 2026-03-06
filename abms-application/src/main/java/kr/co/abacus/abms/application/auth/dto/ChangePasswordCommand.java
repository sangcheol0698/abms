package kr.co.abacus.abms.application.auth.dto;

public record ChangePasswordCommand(
        String username,
        String currentPassword,
        String newPassword
) {

}
