package kr.co.abacus.abms.access;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.abacus.abms.common.audit.AuditNameResolver;

public interface PermissionGroupRepository extends JpaRepository<PermissionGroup, Long>, AuditNameResolver {

    List<PermissionGroup> findAllByOrderByGroupTypeAscNameAsc();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    @Override
    default String auditKind() {
        return "PermissionGroup";
    }

    @Override
    default Map<Long, String> auditNames(Collection<Long> ids) {
        return AuditNameResolver.byId(findAllById(ids), PermissionGroup::id, PermissionGroup::getName);
    }

}
