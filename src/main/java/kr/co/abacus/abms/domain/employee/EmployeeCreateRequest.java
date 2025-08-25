package kr.co.abacus.abms.domain.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EmployeeCreateRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 1, max = 10) String name,
        @NotNull LocalDate joinDate,
        @NotNull LocalDate birthDate,
        @NotNull EmployeePosition position,
        @NotNull EmployeeType type,
        @NotNull EmployeeGrade grade,
        String memo
) {

}
