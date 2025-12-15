package kr.co.abacus.abms.adapter.web.employee.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.employee.dto.EmployeeCreateCommand;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;

public record EmployeeCreateRequest(
    @NotNull UUID departmentId,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 1, max = 10) String name,
    @NotNull LocalDate joinDate,
    @NotNull LocalDate birthDate,
    @NotNull EmployeePosition position,
    @NotNull EmployeeType type,
    @NotNull EmployeeGrade grade,
    @NotNull EmployeeAvatar avatar,
    @Nullable String memo
) {

    public EmployeeCreateCommand toCommand() {
        return EmployeeCreateCommand.builder()
            .departmentId(departmentId)
            .email(email)
            .name(name)
            .joinDate(joinDate)
            .birthDate(birthDate)
            .position(position)
            .type(type)
            .grade(grade)
            .avatar(avatar)
            .memo(memo)
            .build();
    }

}
