package kr.co.abacus.abms.application.required;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.shared.Email;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    boolean existsByEmail(Email email);

    Optional<Employee> findByIdAndDeletedFalse(UUID id);

}
