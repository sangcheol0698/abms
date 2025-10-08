package kr.co.abacus.abms.adapter.webapi.employee;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeCreateResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeGradeResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeePositionResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeStatusResponse;
import kr.co.abacus.abms.adapter.webapi.employee.dto.EmployeeTypeResponse;
import kr.co.abacus.abms.application.department.provided.DepartmentFinder;
import kr.co.abacus.abms.application.employee.provided.EmployeeFinder;
import kr.co.abacus.abms.application.employee.provided.EmployeeManager;
import kr.co.abacus.abms.application.employee.provided.EmployeeSearchRequest;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeCreateRequest;
import kr.co.abacus.abms.domain.employee.EmployeeUpdateRequest;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;
import kr.co.abacus.abms.domain.employee.EmployeeType;

@RequiredArgsConstructor
@RestController
public class EmployeeApi {

    private static final String SYSTEM_DELETER = "SYSTEM";

    private final EmployeeManager employeeManager;
    private final EmployeeFinder employeeFinder;
    private final DepartmentFinder departmentFinder;

    @PostMapping("/api/employees")
    public EmployeeCreateResponse create(@RequestBody @Valid EmployeeCreateRequest request) {
        Employee employee = employeeManager.create(request);

        return EmployeeCreateResponse.of(employee);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/api/employees/{id}")
    public void delete(@PathVariable UUID id) {
        employeeManager.delete(id, SYSTEM_DELETER);
    }

    @PutMapping("/api/employees/{id}")
    public EmployeeResponse update(@PathVariable UUID id, @RequestBody @Valid EmployeeUpdateRequest request) {
        Employee employee = employeeManager.updateInfo(id, request);
        Department department = departmentFinder.find(employee.getDepartmentId());

        return EmployeeResponse.of(employee, department);
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
        List<Department> departments = departmentFinder.findAll(); // TODO: 최적화 방안 고려

        return employees.map(employee -> {
            Department department = getDepartment(employee, departments);
            return EmployeeResponse.of(employee, department);
        });
    }

    @GetMapping("/api/employees/grades")
    public List<EmployeeGradeResponse> getEmployeeGrades() {
        return Arrays.stream(EmployeeGrade.values())
            .map(EmployeeGradeResponse::of)
            .toList();
    }

    @GetMapping("/api/employees/positions")
    public List<EmployeePositionResponse> getEmployeePositions() {
        return Arrays.stream(EmployeePosition.values())
            .map(EmployeePositionResponse::of)
            .toList();
    }

    @GetMapping("/api/employees/types")
    public List<EmployeeTypeResponse> getEmployeeTypes() {
        return Arrays.stream(EmployeeType.values())
            .map(EmployeeTypeResponse::of)
            .toList();
    }

    @GetMapping("/api/employees/statuses")
    public List<EmployeeStatusResponse> getEmployeeStatuses() {
        return Arrays.stream(EmployeeStatus.values())
            .map(EmployeeStatusResponse::of)
            .toList();
    }

    private Department getDepartment(Employee employee, List<Department> departments) {
        return departments.stream()
            .filter(d -> d.getId().equals(employee.getDepartmentId()))
            .findFirst()
            .orElseThrow();
    }

}
