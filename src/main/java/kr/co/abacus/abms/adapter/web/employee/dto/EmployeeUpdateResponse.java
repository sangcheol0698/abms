package kr.co.abacus.abms.adapter.web.employee.dto;

import java.util.UUID;

public record EmployeeUpdateResponse(
    UUID employeeId
) {

    public static EmployeeUpdateResponse of(UUID employeeId) {
        return new EmployeeUpdateResponse(employeeId);
    }

}
