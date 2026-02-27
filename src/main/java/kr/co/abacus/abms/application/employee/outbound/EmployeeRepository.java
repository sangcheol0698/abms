package kr.co.abacus.abms.application.employee.outbound;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;
import kr.co.abacus.abms.domain.shared.Email;

public interface EmployeeRepository extends Repository<Employee, Long>, CustomEmployeeRepository {

    Employee save(Employee employee);

    boolean existsByEmail(Email email);

    Optional<Employee> findByEmailAndDeletedFalse(Email email);

    Optional<Employee> findById(Long id);

    Optional<Employee> findByIdAndDeletedFalse(Long id);

    List<Employee> findAllByIdInAndDeletedFalse(List<Long> leaderIds);

    List<Employee> findAllByDepartmentIdInAndDeletedFalse(List<Long> departmentIds);

    Page<Employee> findAllByDepartmentIdAndDeletedFalse(Long departmentId, Pageable pageable);

    Optional<Employee> findByName(String name);

    int count();

    int countByJoinDateBetween(LocalDate startDate, LocalDate endDate);

    int countByStatus(EmployeeStatus status);

}
