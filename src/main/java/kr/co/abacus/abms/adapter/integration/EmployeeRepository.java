package kr.co.abacus.abms.adapter.integration;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.shared.Email;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmail(Email email);

}
