package kr.co.abacus.abms.adapter.webapi.employee;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeCreateResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeResponse;
import kr.co.abacus.abms.application.department.provided.DepartmentFinder;
import kr.co.abacus.abms.application.employee.provided.EmployeeFinder;
import kr.co.abacus.abms.application.employee.provided.EmployeeManager;
import kr.co.abacus.abms.application.employee.provided.EmployeeSearchRequest;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeCreateRequest;

@RequiredArgsConstructor
@RestController
public class EmployeeApi {

    private final EmployeeManager employeeManager;
    private final EmployeeFinder employeeFinder;
    private final DepartmentFinder departmentFinder;

    @PostMapping("/api/employees")
    public EmployeeCreateResponse create(@RequestBody @Valid EmployeeCreateRequest request) {
        Employee employee = employeeManager.create(request);

        return EmployeeCreateResponse.of(employee);
    }

    @GetMapping("/api/employees/{id}")
    public EmployeeResponse find(@PathVariable UUID id) {
        Employee employee = employeeFinder.find(id);

        Department department = departmentFinder.find(employee.getDepartmentId());

        return EmployeeResponse.of(employee, department);
    }

    @GetMapping("/api/employees")
    public Page<EmployeeResponse> search(@Valid EmployeeSearchRequest request, Pageable pageable) {
        Page<Employee> employees = employeeFinder.search(request, pageable);

        return employees.map(employee -> {
            Department department = departmentFinder.find(employee.getDepartmentId());
            return EmployeeResponse.of(employee, department);
        });
    }

}
