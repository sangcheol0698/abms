package kr.co.abacus.abms.employee;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
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
        @Size(max = 2000, message = "메모는 2000자 이하로 입력하세요.") @Nullable String memo,
        @Positive(message = "연봉은 0보다 커야 합니다.") @Nullable Long annualSalary,
        @Pattern(regexp = "^$|^[0-9+()\\- ]{7,20}$", message = "연락처 형식이 올바르지 않습니다. (예: 010-1234-5678)") @Nullable String phone,
        @PastOrPresent(message = "경력 시작일은 오늘 이후일 수 없습니다.") @Nullable LocalDate careerStartDate,
        @Nullable EmployeeJob job,
        @Size(max = 500, message = "보유 기술은 500자 이하로 입력하세요.") @Nullable String skills,
        @Nullable WorkType workType
) {

    public static EmployeeForm empty() {
        return new EmployeeForm(null, null, null, LocalDate.now(), null, EmployeePosition.ASSOCIATE, EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR, null, null, null, null, EmployeeJob.DEVELOPMENT, null, WorkType.OFFICE);
    }

    public static EmployeeForm of(Employee e) {
        return new EmployeeForm(e.getDepartmentId(), e.getName(), e.getEmail(), e.getJoinDate(), e.getBirthDate(),
                e.getPosition(), e.getType(), e.getGrade(), e.getMemo(), null,
                e.getPhone(), e.getCareerStartDate(), e.getJob(), e.getSkills(), e.getWorkType());
    }

    public EmployeeProfile toProfile() {
        return new EmployeeProfile(departmentId, name, email, joinDate, birthDate, position, type, grade,
                memo, phone, careerStartDate, job, skills, workType);
    }

}
