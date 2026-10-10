package kr.co.abacus.abms.site;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.abacus.abms.common.audit.AuditNameResolver;

public interface SiteRepository extends JpaRepository<Site, Long>, AuditNameResolver {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);

    List<Site> findAllByOrderByNameAsc();

    @Override
    default String auditKind() {
        return "Site";
    }

    @Override
    default Map<Long, String> auditNames(Collection<Long> ids) {
        return AuditNameResolver.byId(findAllById(ids), Site::id, Site::getName);
    }

}
