package kr.co.abacus.abms.application.employee.required;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import kr.co.abacus.abms.application.employee.dto.EmployeeResponse;
import kr.co.abacus.abms.application.employee.provided.EmployeeSearchRequest;
import kr.co.abacus.abms.domain.employee.Employee;

public interface CustomEmployeeRepository {

    Page<EmployeeResponse> search(EmployeeSearchRequest request, Pageable pageable);

    List<Employee> search(EmployeeSearchRequest request);

}
