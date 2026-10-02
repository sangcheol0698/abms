package kr.co.abacus.abms.employee;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

/**
 * 본인 정보 수정 폼 (SELF 권한).
 */
public record OwnProfileForm(
        @NotBlank(message = "이름을 입력하세요.") @Size(max = 30) @Nullable String name,
        @NotNull(message = "생년월일을 입력하세요.") @Past @Nullable LocalDate birthDate,
        @NotNull(message = "아바타를 선택하세요.") @Nullable EmployeeAvatar avatar
) {

    public static OwnProfileForm of(Employee employee) {
        return new OwnProfileForm(employee.getName(), employee.getBirthDate(), employee.getAvatar());
    }

}
