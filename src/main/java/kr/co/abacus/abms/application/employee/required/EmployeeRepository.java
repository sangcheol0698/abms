package kr.co.abacus.abms.application.employee.required;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.shared.Email;

public interface EmployeeRepository extends Repository<Employee, UUID>, CustomEmployeeRepository {

    Employee save(Employee employee);

    boolean existsByEmail(Email email);

    Optional<Employee> findById(UUID id);

    Optional<Employee> findByIdAndDeletedFalse(UUID id);

    boolean existsByEmailAndIdNot(Email email, UUID employeeId);

    List<Employee> findAllByIdInAndDeletedFalse(List<UUID> leaderIds);

    List<Employee> findAllByDepartmentIdInAndDeletedFalse(List<UUID> departmentIds);

}
