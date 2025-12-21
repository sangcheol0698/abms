package kr.co.abacus.abms.domain.account;

public record AccountCreateRequest(
        Long employeeId,
        String username,
        String password) {

}
