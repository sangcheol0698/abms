package kr.co.abacus.abms.department;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.abacus.abms.common.audit.AuditNameResolver;
import kr.co.abacus.abms.security.DepartmentHierarchy;

public interface DepartmentRepository extends JpaRepository<Department, Long>, AuditNameResolver, DepartmentHierarchy {

    List<Department> findAllByParentId(Long parentId);

    boolean existsByCode(String code);

    List<Department> findAllBySiteId(Long siteId);

    boolean existsBySiteId(Long siteId);

    List<Department> findAllBySiteIdIsNotNull();

    @Override
    default String auditKind() {
        return "Department";
    }

    @Override
    default Map<Long, String> auditNames(Collection<Long> ids) {
        return AuditNameResolver.byId(findAllById(ids), Department::id, Department::getName);
    }

    @Override
    default Set<Long> subtreeIds(Long departmentId) {
        return new DepartmentTree(findAll()).subtreeIds(departmentId);
    }

}
