package kr.co.abacus.abms.adapter.webapi.employee;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeResponse;
import kr.co.abacus.abms.application.provided.DepartmentFinder;
import kr.co.abacus.abms.application.provided.EmployeeCreator;
import kr.co.abacus.abms.application.provided.EmployeeFinder;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeCreateRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class EmployeeApi {

    private final EmployeeCreator employeeCreator;
    private final EmployeeFinder employeeFinder;
    private final DepartmentFinder departmentFinder;

    @PostMapping("/employees")
    public EmployeeResponse createEmployee(@RequestBody @Valid EmployeeCreateRequest request) {
        Department department = departmentFinder.find(request.departmentId());

        Employee employee = employeeCreator.create(request);

        return EmployeeResponse.of(employee, department);
    }

    @GetMapping("/employees/{id}")
    public EmployeeResponse getEmployee(@PathVariable UUID id) {
        Employee employee = employeeFinder.find(id);

        Department department = departmentFinder.find(employee.getDepartmentId());

        return EmployeeResponse.of(employee, department);
    }

}
