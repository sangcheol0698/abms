package kr.co.abacus.abms.employee;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

/**
 * 직원 생성/수정 시 입력되는 프로필 값.
 */
public record EmployeeProfile(
        Long departmentId,
        String name,
        String email,
        LocalDate joinDate,
        LocalDate birthDate,
        EmployeePosition position,
        EmployeeType type,
        EmployeeGrade grade,
        EmployeeAvatar avatar,
        @Nullable String memo
) {
}
