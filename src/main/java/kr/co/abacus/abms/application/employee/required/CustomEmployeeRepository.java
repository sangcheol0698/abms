package kr.co.abacus.abms.application.employee.required;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.co.abacus.abms.application.employee.provided.EmployeeSearchRequest;
import kr.co.abacus.abms.domain.employee.Employee;

public interface CustomEmployeeRepository {

    Page<Employee> search(EmployeeSearchRequest request, Pageable pageable);

}
