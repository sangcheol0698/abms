package kr.co.abacus.abms.application.auth.dto;

public record LoginCommand(
        String username,
        String password
) {

}
