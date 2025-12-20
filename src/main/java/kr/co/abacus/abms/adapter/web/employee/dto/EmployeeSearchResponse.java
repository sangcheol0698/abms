package kr.co.abacus.abms.adapter.web.employee.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import lombok.Builder;

import kr.co.abacus.abms.adapter.web.EnumResponse;
import kr.co.abacus.abms.application.employee.dto.EmployeeSummary;

@Builder
public record EmployeeSearchResponse(
    Long departmentId,
    String departmentName,
    Long employeeId,
    String name,
    String email,
    LocalDate joinDate,
    LocalDate birthDate,
    EnumResponse position,
    EnumResponse status,
    EnumResponse grade,
    EnumResponse type,
    EnumResponse avatar,
    @Nullable String memo) {

    public static EmployeeSearchResponse of(EmployeeSummary summary) {
        return EmployeeSearchResponse.builder()
            .departmentId(summary.departmentId())
            .departmentName(summary.departmentName())
            .employeeId(summary.employeeId())
            .name(summary.name())
            .email(summary.email().address())
            .joinDate(summary.joinDate())
            .birthDate(summary.birthDate())
            .position(new EnumResponse(summary.position().name(), summary.position().getDescription()))
            .status(new EnumResponse(summary.status().name(), summary.status().getDescription()))
            .grade(new EnumResponse(summary.grade().name(), summary.grade().getDescription()))
            .type(new EnumResponse(summary.type().name(), summary.type().getDescription()))
            .avatar(new EnumResponse(summary.avatar().name(), summary.avatar().getDescription()))
            .memo(summary.memo())
            .build();
    }

}
