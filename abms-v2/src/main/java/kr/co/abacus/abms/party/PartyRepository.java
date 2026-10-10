package kr.co.abacus.abms.party;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.abacus.abms.common.audit.AuditNameResolver;

public interface PartyRepository extends JpaRepository<Party, Long>, AuditNameResolver {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    Page<Party> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Party> findAllByOrderByNameAsc();

    @Override
    default String auditKind() {
        return "Party";
    }

    @Override
    default Map<Long, String> auditNames(Collection<Long> ids) {
        return AuditNameResolver.byId(findAllById(ids), Party::id, Party::getName);
    }

}
