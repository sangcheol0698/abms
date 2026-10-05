package kr.co.abacus.abms.employee;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

/**
 * 본인 정보 수정 폼 (SELF 권한).
 */
public record OwnProfileForm(
        @NotBlank(message = "이름을 입력하세요.") @Size(max = 30) @Nullable String name,
        @NotNull(message = "생년월일을 입력하세요.") @Past @Nullable LocalDate birthDate,
        @Pattern(regexp = "^$|^[0-9+()\\- ]{7,20}$", message = "연락처 형식이 올바르지 않습니다. (예: 010-1234-5678)") @Nullable String phone,
        @Size(max = 500, message = "보유 기술은 500자 이하로 입력하세요.") @Nullable String skills
) {

    public static OwnProfileForm of(Employee employee) {
        return new OwnProfileForm(employee.getName(), employee.getBirthDate(), employee.getPhone(), employee.getSkills());
    }

}
