package kr.co.abacus.abms.adapter.webapi.employee;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeCreateResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeResponse;
import kr.co.abacus.abms.application.department.provided.DepartmentFinder;
import kr.co.abacus.abms.application.employee.provided.EmployeeCreator;
import kr.co.abacus.abms.application.employee.provided.EmployeeFinder;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeCreateRequest;

@RequiredArgsConstructor
@RestController
public class EmployeeApi {

    private final EmployeeCreator employeeCreator;
    private final EmployeeFinder employeeFinder;
    private final DepartmentFinder departmentFinder;

    @PostMapping("/api/employees")
    public EmployeeCreateResponse create(@RequestBody @Valid EmployeeCreateRequest request) {
        Employee employee = employeeCreator.create(request);

        return EmployeeCreateResponse.of(employee);
    }

    @GetMapping("/api/employees/{id}")
    public EmployeeResponse find(@PathVariable UUID id) {
        Employee employee = employeeFinder.find(id);

        Department department = departmentFinder.find(employee.getDepartmentId());

        return EmployeeResponse.of(employee, department);
    }

}
