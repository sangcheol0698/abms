package kr.co.abacus.abms.application.employee;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.employee.dto.EmployeeResponse;
import kr.co.abacus.abms.application.department.provided.DepartmentFinder;
import kr.co.abacus.abms.application.employee.provided.EmployeeFinder;
import kr.co.abacus.abms.application.employee.provided.EmployeeSearchRequest;
import kr.co.abacus.abms.application.employee.required.EmployeeRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeNotFoundException;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class EmployeeQueryService implements EmployeeFinder {

    private final EmployeeRepository employeeRepository;
    private final DepartmentFinder departmentFinder;

    @Override
    public Employee find(UUID id) {
        return employeeRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new EmployeeNotFoundException("존재하지 않는 직원입니다: " + id));
    }

    @Override
    public EmployeeResponse findWithDepartment(UUID id) {
        Employee employee = find(id);

        Department department = departmentFinder.find(employee.getDepartmentId());

        return EmployeeResponse.of(employee, department);
    }

    @Override
    public Page<EmployeeResponse> search(EmployeeSearchRequest request, Pageable pageable) {
        return employeeRepository.search(request, pageable);
    }

}
