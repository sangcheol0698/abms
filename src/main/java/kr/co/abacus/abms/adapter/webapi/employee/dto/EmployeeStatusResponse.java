package kr.co.abacus.abms.adapter.webapi.employee.dto;

import kr.co.abacus.abms.domain.employee.EmployeeStatus;

public record EmployeeStatusResponse(
    String name,
    String description
) {

    public static EmployeeStatusResponse of(EmployeeStatus status) {
        return new EmployeeStatusResponse(status.name(), status.getDescription());
    }

}
