package kr.co.abacus.abms.account;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.abacus.abms.common.audit.AuditNameResolver;

public interface AccountRepository extends JpaRepository<Account, Long>, AuditNameResolver {

    Optional<Account> findByUsername(String username);

    Optional<Account> findByEmployeeId(Long employeeId);

    boolean existsByEmployeeId(Long employeeId);

    boolean existsByUsername(String username);

    List<Account> findAllByOrderByUsernameAsc();

    @Override
    default String auditKind() {
        return "Account";
    }

    @Override
    default Map<Long, String> auditNames(Collection<Long> ids) {
        return AuditNameResolver.byId(findAllById(ids), Account::id, Account::getUsername);
    }

}
