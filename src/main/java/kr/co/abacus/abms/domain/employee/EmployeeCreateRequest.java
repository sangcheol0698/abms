package kr.co.abacus.abms.domain.employee;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

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

}
