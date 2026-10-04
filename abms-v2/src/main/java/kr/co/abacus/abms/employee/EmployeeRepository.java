package kr.co.abacus.abms.employee;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByIdAndDeletedFalse(Long id);

    boolean existsByEmailAndDeletedFalse(String email);

    boolean existsByEmail(String email);

    List<Employee> findAllByIdInAndDeletedFalse(Collection<Long> ids);

    List<Employee> findAllByDepartmentIdAndDeletedFalse(Long departmentId);

    List<Employee> findAllByDepartmentIdInAndDeletedFalse(java.util.Collection<Long> departmentIds);

    List<Employee> findAllByStatusAndDeletedFalse(EmployeeStatus status);

    List<Employee> findAllByDeletedFalseOrderByNameAsc();

}
