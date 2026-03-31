package kr.co.abacus.abms.adapter.api.employee.dto;

import jakarta.validation.constraints.NotNull;

import kr.co.abacus.abms.domain.employee.EmployeeType;

public record EmployeeEmploymentTypeConvertRequest(
        @NotNull EmployeeType type) {

}
