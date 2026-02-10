package kr.co.abacus.abms.adapter.api.employee.dto;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;

public record EmployeePositionUpdateRequest(
        EmployeePosition position,
        @Nullable EmployeeGrade grade) {

}
