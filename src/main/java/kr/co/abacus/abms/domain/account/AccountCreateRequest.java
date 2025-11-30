package kr.co.abacus.abms.domain.account;

import java.util.UUID;

public record AccountCreateRequest(
    UUID employeeId,
    String username,
    String password
) {
}
