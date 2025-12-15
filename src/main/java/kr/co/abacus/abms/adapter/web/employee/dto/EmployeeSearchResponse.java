package kr.co.abacus.abms.adapter.web.employee.dto;

import java.time.LocalDate;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import lombok.Builder;

import kr.co.abacus.abms.adapter.web.EnumResponse;
import kr.co.abacus.abms.application.employee.dto.EmployeeSummary;

@Builder
public record EmployeeSearchResponse(
    UUID departmentId,
    String departmentName,
    UUID employeeId,
    String name,
    String email,
    LocalDate joinDate,
    LocalDate birthDate,
    String position,
    String status,
    String grade,
    String type,
    EnumResponse avatar,
    @Nullable String memo
) {

    public static EmployeeSearchResponse of(EmployeeSummary summary) {
        return EmployeeSearchResponse.builder()
            .departmentId(summary.departmentId())
            .departmentName(summary.departmentName())
            .employeeId(summary.employeeId())
            .name(summary.name())
            .email(summary.email().address())
            .joinDate(summary.joinDate())
            .birthDate(summary.birthDate())
            .position(summary.position().getDescription())
            .status(summary.status().getDescription())
            .grade(summary.grade().getDescription())
            .type(summary.type().getDescription())
            .avatar(new EnumResponse(summary.avatar().name(), summary.avatar().getDescription()))
            .memo(summary.memo())
            .build();
    }

}
