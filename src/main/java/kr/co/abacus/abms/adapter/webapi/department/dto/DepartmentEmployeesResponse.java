package kr.co.abacus.abms.adapter.webapi.department.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import kr.co.abacus.abms.domain.employee.Employee;

public record DepartmentEmployeesResponse(
    List<EmployeeResponse> content,
    long totalElements,
    int totalPages,
    int currentPage,
    int pageSize,
    boolean hasNext,
    boolean hasPrevious
) {

    public static DepartmentEmployeesResponse from(Page<Employee> page) {
        List<EmployeeResponse> content = page.getContent().stream()
            .map(EmployeeResponse::from)
            .toList();

        return new DepartmentEmployeesResponse(
            content,
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize(),
            page.hasNext(),
            page.hasPrevious()
        );
    }
}
