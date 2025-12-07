package kr.co.abacus.abms.adapter.webapi.department.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import kr.co.abacus.abms.application.employee.dto.EmployeeResponse;

public record DepartmentEmployeesResponse(
    List<EmployeeResponse> content,
    long totalElements,
    int totalPages,
    int currentPage,
    int pageSize,
    boolean hasNext,
    boolean hasPrevious
) {

    public static DepartmentEmployeesResponse from(Page<EmployeeResponse> page) {
        return new DepartmentEmployeesResponse(
            page.getContent(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize(),
            page.hasNext(),
            page.hasPrevious()
        );
    }
}
