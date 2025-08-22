package kr.co.abacus.abms.adapter.integration;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.shared.Email;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    boolean existsByEmail(Email email);

}
