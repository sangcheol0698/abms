package kr.co.abacus.abms.adapter.web.department.dto;

import java.util.UUID;

import lombok.Builder;

import kr.co.abacus.abms.application.department.dto.DepartmentDetail;
import kr.co.abacus.abms.domain.department.DepartmentType;

@Builder
public record DepartmentDetailResponse(
    UUID id,
    String code,
    String name,
    DepartmentType type,
    UUID leaderEmployeeId,
    String leaderEmployeeName,
    UUID parentDepartmentId,
    String parentDepartmentName,
    int employeeCount
) {

    public static DepartmentDetailResponse of(DepartmentDetail detail) {
        return DepartmentDetailResponse.builder()
            .id(detail.id())
            .code(detail.code())
            .name(detail.name())
            .type(detail.type())
            .leaderEmployeeId(detail.leaderEmployeeId())
            .leaderEmployeeName(detail.leaderEmployeeName())
            .parentDepartmentId(detail.parentDepartmentId())
            .parentDepartmentName(detail.parentDepartmentName())
            .employeeCount(detail.employeeCount())
            .build();
    }

}
