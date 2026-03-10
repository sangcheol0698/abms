package kr.co.abacus.abms.adapter.api.employee.dto;

import java.time.LocalDate;

import lombok.Builder;

import kr.co.abacus.abms.adapter.api.common.EnumResponse;
import kr.co.abacus.abms.application.employee.dto.EmployeeSummary;

@Builder
public record EmployeeSearchResponse(
        Long departmentId,
        String departmentName,
        Long employeeId,
        String name,
        String email,
        LocalDate joinDate,
        EnumResponse position,
        EnumResponse status,
        EnumResponse grade,
        EnumResponse type,
        EnumResponse avatar) {

    public static EmployeeSearchResponse of(EmployeeSummary summary) {
        return EmployeeSearchResponse.builder()
                .departmentId(summary.departmentId())
                .departmentName(summary.departmentName())
                .employeeId(summary.employeeId())
                .name(summary.name())
                .email(summary.email().address())
                .joinDate(summary.joinDate())
                .position(new EnumResponse(summary.position().name(), summary.position().getDescription(), summary.position().getLevel()))
                .status(new EnumResponse(summary.status().name(), summary.status().getDescription(), summary.status().ordinal()))
                .grade(new EnumResponse(summary.grade().name(), summary.grade().getDescription(), summary.grade().getLevel()))
                .type(new EnumResponse(summary.type().name(), summary.type().getDescription(), summary.type().ordinal()))
                .avatar(new EnumResponse(summary.avatar().name(), summary.avatar().getDescription(), summary.avatar().ordinal()))
                .build();
    }

}
