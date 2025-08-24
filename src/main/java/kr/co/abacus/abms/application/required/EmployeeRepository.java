package kr.co.abacus.abms.application.required;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.shared.Email;

public interface EmployeeRepository extends Repository<Employee, UUID> {

    Employee save(Employee employee);

    boolean existsByEmail(Email email);

    Optional<Employee> findById(UUID id);

    Optional<Employee> findByIdAndDeletedFalse(UUID id);

}
