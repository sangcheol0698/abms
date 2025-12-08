package kr.co.abacus.abms.application.employee.provided;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.co.abacus.abms.application.employee.dto.EmployeeResponse;
import kr.co.abacus.abms.domain.employee.Employee;

/**
 * 직원 조회
 */
public interface EmployeeFinder {

    Employee find(UUID id);

    EmployeeResponse findWithDepartment(UUID id);

    Page<EmployeeResponse> search(EmployeeSearchRequest request, Pageable pageable);

}
