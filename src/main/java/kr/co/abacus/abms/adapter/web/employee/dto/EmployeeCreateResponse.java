package kr.co.abacus.abms.adapter.web.employee.dto;

import java.util.UUID;

public record EmployeeCreateResponse(UUID employeeId) {

    public static EmployeeCreateResponse of(UUID employeeId) {
        return new EmployeeCreateResponse(employeeId);
    }

}
