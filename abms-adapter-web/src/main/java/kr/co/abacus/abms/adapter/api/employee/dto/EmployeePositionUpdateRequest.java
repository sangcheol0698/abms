package kr.co.abacus.abms.adapter.api.employee.dto;

import jakarta.validation.constraints.NotNull;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;

public record EmployeePositionUpdateRequest(
        @NotNull EmployeePosition position,
        @Nullable EmployeeGrade grade) {

}
