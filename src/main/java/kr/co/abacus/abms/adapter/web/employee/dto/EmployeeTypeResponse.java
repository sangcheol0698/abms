package kr.co.abacus.abms.adapter.web.employee.dto;

import kr.co.abacus.abms.domain.employee.EmployeeType;

public record EmployeeTypeResponse(
    String name,
    String description
) {

    public static EmployeeTypeResponse of(EmployeeType type) {
        return new EmployeeTypeResponse(type.name(), type.getDescription());
    }

}
