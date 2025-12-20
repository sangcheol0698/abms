package kr.co.abacus.abms.adapter.web.employee.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import kr.co.abacus.abms.application.employee.dto.EmployeeUpdateCommand;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;

public record EmployeeUpdateRequest(
    @NotNull Long departmentId,
    @NotBlank @Email(message = "이메일 형식이 아닙니다.") String email,
    @NotBlank @Size(min = 1, max = 10) String name,
    @NotNull LocalDate joinDate,
    @NotNull LocalDate birthDate,
    @NotNull EmployeePosition position,
    @NotNull EmployeeType type,
    @NotNull EmployeeGrade grade,
    @NotNull EmployeeAvatar avatar,
    String memo) {

    public EmployeeUpdateCommand toCommand() {
        return new EmployeeUpdateCommand(
            departmentId,
            email,
            name,
            joinDate,
            birthDate,
            position,
            type,
            grade,
            avatar,
            memo);
    }

}
