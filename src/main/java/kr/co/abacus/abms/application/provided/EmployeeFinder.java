package kr.co.abacus.abms.application.provided;

import java.util.UUID;

import kr.co.abacus.abms.domain.employee.Employee;

/**
 * 직원 조회
 */
public interface EmployeeFinder {

    Employee find(UUID id);

}
