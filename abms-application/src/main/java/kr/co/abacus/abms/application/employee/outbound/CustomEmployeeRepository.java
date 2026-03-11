package kr.co.abacus.abms.application.employee.outbound;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.co.abacus.abms.application.employee.authorization.EmployeeReadScope;
import kr.co.abacus.abms.application.employee.dto.EmployeeDetail;
import kr.co.abacus.abms.application.employee.dto.EmployeeOverviewSummary;
import kr.co.abacus.abms.application.employee.dto.EmployeeSearchCondition;
import kr.co.abacus.abms.application.employee.dto.EmployeeSummary;
import kr.co.abacus.abms.domain.employee.Employee;

public interface CustomEmployeeRepository {

    Page<EmployeeSummary> search(EmployeeSearchCondition condition, Pageable pageable);

    EmployeeOverviewSummary summarize(EmployeeSearchCondition condition);

    List<Employee> search(EmployeeSearchCondition condition);

    List<Employee> search(EmployeeSearchCondition condition, EmployeeReadScope scope);

    @Nullable
    EmployeeDetail findEmployeeDetail(Long id);

}
