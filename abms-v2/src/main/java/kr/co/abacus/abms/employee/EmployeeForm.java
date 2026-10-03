package kr.co.abacus.abms.employee;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

/**
 * 직원 생성/수정 폼.
 *
 * @param annualSalary 생성 시 초기 연봉 (선택)
 */
public record EmployeeForm(
        @NotNull(message = "부서를 선택하세요.") @Nullable Long departmentId,
        @NotBlank(message = "이름을 입력하세요.") @Size(max = 30, message = "이름은 30자 이하로 입력하세요.") @Nullable String name,
        @NotBlank(message = "이메일을 입력하세요.") @Email(message = "이메일 형식이 올바르지 않습니다.") @Nullable String email,
        @NotNull(message = "입사일을 입력하세요.") @Nullable LocalDate joinDate,
        @NotNull(message = "생년월일을 입력하세요.") @Past(message = "생년월일은 오늘 이전이어야 합니다.") @Nullable LocalDate birthDate,
        @NotNull(message = "직급을 선택하세요.") @Nullable EmployeePosition position,
        @NotNull(message = "고용유형을 선택하세요.") @Nullable EmployeeType type,
        @NotNull(message = "등급을 선택하세요.") @Nullable EmployeeGrade grade,
        @Nullable EmployeeAvatar avatar,
        @Size(max = 2000, message = "메모는 2000자 이하로 입력하세요.") @Nullable String memo,
        @Positive(message = "연봉은 0보다 커야 합니다.") @Nullable Long annualSalary
) {

    public static EmployeeForm empty() {
        return new EmployeeForm(null, null, null, LocalDate.now(), null, EmployeePosition.ASSOCIATE, EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR, EmployeeAvatar.SKY_GLOW, null, null);
    }

    public static EmployeeForm of(Employee e) {
        return new EmployeeForm(e.getDepartmentId(), e.getName(), e.getEmail(), e.getJoinDate(), e.getBirthDate(),
                e.getPosition(), e.getType(), e.getGrade(), e.getAvatar(), e.getMemo(), null);
    }

    public EmployeeProfile toProfile() {
        return new EmployeeProfile(departmentId, name, email, joinDate, birthDate, position, type, grade,
                avatar == null ? EmployeeAvatar.SKY_GLOW : avatar, memo);
    }

}
