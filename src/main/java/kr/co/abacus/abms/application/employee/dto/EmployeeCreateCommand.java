package kr.co.abacus.abms.application.employee.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import lombok.Builder;

import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;

@Builder
public record EmployeeCreateCommand(
        Long departmentId,
        String email,
        String name,
        LocalDate joinDate,
        LocalDate birthDate,
        EmployeePosition position,
        EmployeeType type,
        EmployeeGrade grade,
        EmployeeAvatar avatar,
        @Nullable String memo) {

    public Employee toEntity() {
        return Employee.create(
                departmentId,
                name,
                email,
                joinDate,
                birthDate,
                position,
                type,
                grade,
                avatar,
                memo);
    }

}
