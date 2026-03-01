package kr.co.abacus.abms.adapter.infrastructure.auth;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.account.Account;

public interface AccountRepository
        extends Repository<Account, Long>,
        kr.co.abacus.abms.application.auth.outbound.AccountRepository {
}
