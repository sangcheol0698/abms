package kr.co.abacus.abms.domain.department;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

public record DepartmentCreateRequest(
        @NotBlank @Size(max = 32) String code,
        @NotBlank @Size(max = 30) String name,
        @NotNull DepartmentType type,
        @Nullable UUID leaderEmployeeId
) {

}
