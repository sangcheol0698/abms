package kr.co.abacus.abms.application.auth.outbound;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.shared.Email;

public interface AccountRepository extends Repository<Account, Long> {

    Account save(Account account);

    Optional<Account> findByUsername(Email username);

}
