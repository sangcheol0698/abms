package kr.co.abacus.abms.adapter.web.employee.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Builder;

import kr.co.abacus.abms.application.employee.dto.EmployeeDetail;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeStatus;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.shared.Email;

@Builder
public record EmployeeDetailResponse(
    UUID departmentId,
    String departmentName,
    UUID employeeId,
    String name,
    Email email,
    LocalDate joinDate,
    LocalDate birthDate,
    EmployeePosition position,
    EmployeeStatus status,
    EmployeeGrade grade,
    EmployeeType type,
    EmployeeAvatar avatar,
    String memo
) {

    public static EmployeeDetailResponse of(EmployeeDetail detail) {
        return EmployeeDetailResponse.builder()
            .departmentId(detail.departmentId())
            .departmentName(detail.departmentName())
            .employeeId(detail.employeeId())
            .name(detail.name())
            .email(detail.email())
            .joinDate(detail.joinDate())
            .birthDate(detail.birthDate())
            .position(detail.position())
            .status(detail.status())
            .grade(detail.grade())
            .type(detail.type())
            .avatar(detail.avatar())
            .memo(detail.memo())
            .build();
    }

}
