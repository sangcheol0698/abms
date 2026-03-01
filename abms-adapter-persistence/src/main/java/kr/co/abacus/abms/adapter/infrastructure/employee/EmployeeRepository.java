package kr.co.abacus.abms.adapter.infrastructure.employee;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.employee.Employee;

public interface EmployeeRepository
        extends Repository<Employee, Long>,
        kr.co.abacus.abms.application.employee.outbound.EmployeeRepository {
}
