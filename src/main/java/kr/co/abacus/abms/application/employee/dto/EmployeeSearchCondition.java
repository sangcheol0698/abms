package kr.co.abacus.abms.application.employee.dto;

import java.util.List;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;
import kr.co.abacus.abms.domain.employee.EmployeeType;

public record EmployeeSearchCondition(
    @Nullable String name,
    @Nullable List<EmployeePosition> positions,
    @Nullable List<EmployeeType> types,
    @Nullable List<EmployeeStatus> statuses,
    @Nullable List<EmployeeGrade> grades,
    @Nullable List<Long> departmentIds) {

}
