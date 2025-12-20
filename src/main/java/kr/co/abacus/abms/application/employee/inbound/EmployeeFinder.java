package kr.co.abacus.abms.application.employee.inbound;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.co.abacus.abms.application.employee.dto.EmployeeSearchCondition;
import kr.co.abacus.abms.application.employee.dto.EmployeeDetail;
import kr.co.abacus.abms.application.employee.dto.EmployeeSummary;
import kr.co.abacus.abms.domain.employee.Employee;

/**
 * 직원 조회
 */
public interface EmployeeFinder {

    Employee find(Long id);

    EmployeeDetail findEmployeeDetail(Long id);

    Page<EmployeeSummary> search(EmployeeSearchCondition condition, Pageable pageable);

}
