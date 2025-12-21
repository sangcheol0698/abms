package kr.co.abacus.abms.application.employee.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.shared.Email;

public record EmployeeSummary(
        Long departmentId,
        String departmentName,
        Long employeeId,
        String name,
        Email email,
        LocalDate joinDate,
        LocalDate birthDate,
        EmployeePosition position,
        EmployeeStatus status,
        EmployeeGrade grade,
        EmployeeType type,
        EmployeeAvatar avatar,
        @Nullable String memo) {

}
