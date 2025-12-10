package kr.co.abacus.abms.adapter.web.employee.dto;

import java.util.UUID;

import kr.co.abacus.abms.domain.employee.Employee;

public record EmployeeCreateResponse(
    UUID employeeId,
    String email
) {

    public static EmployeeCreateResponse of(Employee employee) {
        return new EmployeeCreateResponse(
            employee.getId(),
            employee.getEmail().address()
        );
    }

}
