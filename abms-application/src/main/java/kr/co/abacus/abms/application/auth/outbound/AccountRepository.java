package kr.co.abacus.abms.application.auth.outbound;

import java.util.Optional;

import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.shared.Email;

public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findByUsername(Email username);

}
