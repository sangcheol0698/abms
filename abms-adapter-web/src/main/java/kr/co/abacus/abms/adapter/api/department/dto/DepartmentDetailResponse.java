package kr.co.abacus.abms.adapter.api.department.dto;

import lombok.Builder;

import kr.co.abacus.abms.application.department.dto.DepartmentDetail;
import kr.co.abacus.abms.domain.department.DepartmentType;

@Builder
public record DepartmentDetailResponse(
        Long id,
        String code,
        String name,
        DepartmentType type,
        Long leaderEmployeeId,
        String leaderEmployeeName,
        Long parentDepartmentId,
        String parentDepartmentName,
        int employeeCount) {

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
