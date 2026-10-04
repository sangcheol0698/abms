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
        @Nullable String memo,
        @Nullable String phone,
        @Nullable LocalDate careerStartDate,
        @Nullable EmployeeJob job,
        @Nullable String skills,
        @Nullable WorkType workType
) {

    /** 기본 인사 정보만으로 만드는 프로필 (연락처·직무·기술 등은 비워 둔다) */
    public EmployeeProfile(Long departmentId, String name, String email, LocalDate joinDate, LocalDate birthDate,
                           EmployeePosition position, EmployeeType type, EmployeeGrade grade, EmployeeAvatar avatar,
                           @Nullable String memo) {
        this(departmentId, name, email, joinDate, birthDate, position, type, grade, avatar, memo, null, null, null, null, null);
    }

}
