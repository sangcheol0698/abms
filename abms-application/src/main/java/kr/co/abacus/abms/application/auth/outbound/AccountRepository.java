package kr.co.abacus.abms.application.auth.outbound;

import java.util.Optional;
import java.util.List;

import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.shared.Email;

public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(Long id);

    Optional<Account> findByIdAndDeletedFalse(Long id);

    List<Account> findAllByIdInAndDeletedFalse(List<Long> ids);

    List<Account> findAllByDeletedFalse();

    Optional<Account> findByUsername(Email username);

}
