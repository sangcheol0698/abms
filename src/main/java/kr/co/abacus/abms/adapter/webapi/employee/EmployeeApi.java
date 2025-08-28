package kr.co.abacus.abms.adapter.webapi.employee;

import jakarta.validation.Valid;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeCreateResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeResponse;
import kr.co.abacus.abms.application.provided.DepartmentFinder;
import kr.co.abacus.abms.application.provided.EmployeeCreator;
import kr.co.abacus.abms.application.provided.EmployeeFinder;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
