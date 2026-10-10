package kr.co.abacus.abms.employee;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import kr.co.abacus.abms.common.audit.AuditNameResolver;
import kr.co.abacus.abms.security.LoginProfileLookup;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee>, AuditNameResolver, LoginProfileLookup {

    Optional<Employee> findByIdAndDeletedFalse(Long id);

    boolean existsByEmailAndDeletedFalse(String email);

    boolean existsByEmail(String email);

    List<Employee> findAllByIdInAndDeletedFalse(Collection<Long> ids);

    List<Employee> findAllByDepartmentIdAndDeletedFalse(Long departmentId);

    long countByStatusAndDeletedFalse(EmployeeStatus status);

    List<Employee> findAllByDepartmentIdInAndDeletedFalse(java.util.Collection<Long> departmentIds);

    List<Employee> findAllByStatusAndDeletedFalse(EmployeeStatus status);

    List<Employee> findAllByDeletedFalseOrderByNameAsc();

    @Override
    default String auditKind() {
        return "Employee";
    }

    @Override
    default Map<Long, String> auditNames(Collection<Long> ids) {
        return AuditNameResolver.byId(findAllById(ids), Employee::id, Employee::getName);
    }

    @Override
    default Optional<LoginProfile> findLoginProfile(Long employeeId) {
        return findByIdAndDeletedFalse(employeeId).map(employee -> new LoginProfile(employee.id(), employee.getDepartmentId(),
                employee.getName(), employee.photoUrl(), employee.isResigned()));
    }

}
