package kr.co.abacus.abms.adapter.web.employee.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import lombok.Builder;

import kr.co.abacus.abms.adapter.web.EnumResponse;
import kr.co.abacus.abms.application.employee.dto.EmployeeDetail;
import kr.co.abacus.abms.domain.shared.Email;

@Builder
public record EmployeeDetailResponse(
        Long departmentId,
        String departmentName,
        Long employeeId,
        String name,
        Email email,
        LocalDate joinDate,
        LocalDate birthDate,
        EnumResponse position,
        EnumResponse status,
        EnumResponse grade,
        EnumResponse type,
        EnumResponse avatar,
        @Nullable String memo) {

    public static EmployeeDetailResponse of(EmployeeDetail detail) {
        return EmployeeDetailResponse.builder()
                .departmentId(detail.departmentId())
                .departmentName(detail.departmentName())
                .employeeId(detail.employeeId())
                .name(detail.name())
                .email(detail.email())
                .joinDate(detail.joinDate())
                .birthDate(detail.birthDate())
                .position(new EnumResponse(detail.position().name(), detail.position().getDescription()))
                .status(new EnumResponse(detail.status().name(), detail.status().getDescription()))
                .grade(new EnumResponse(detail.grade().name(), detail.grade().getDescription()))
                .type(new EnumResponse(detail.type().name(), detail.type().getDescription()))
                .avatar(new EnumResponse(detail.avatar().name(), detail.avatar().getDescription()))
                .memo(detail.memo())
                .build();
    }

}
