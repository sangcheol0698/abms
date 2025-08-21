package kr.co.abacus.abms.application.provided;

import jakarta.validation.Valid;

import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeCreateRequest;

/**
 * 직원 생성
 */
public interface EmployeeCreator {

    Employee create(@Valid EmployeeCreateRequest request);

}
