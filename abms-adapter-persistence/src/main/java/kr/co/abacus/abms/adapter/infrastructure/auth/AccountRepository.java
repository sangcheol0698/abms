package kr.co.abacus.abms.adapter.infrastructure.auth;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.account.Account;

public interface AccountRepository
        extends Repository<Account, Long>,
        kr.co.abacus.abms.application.auth.outbound.AccountRepository {

    @Override
    Account save(Account account);

    @Override
    java.util.Optional<Account> findById(Long id);

    @Override
    java.util.Optional<Account> findByIdAndDeletedFalse(Long id);

    @Override
    java.util.List<Account> findAllByIdInAndDeletedFalse(java.util.List<Long> ids);

    @Override
    java.util.List<Account> findAllByDeletedFalse();

    @Override
    java.util.Optional<Account> findByUsername(kr.co.abacus.abms.domain.shared.Email username);
}
