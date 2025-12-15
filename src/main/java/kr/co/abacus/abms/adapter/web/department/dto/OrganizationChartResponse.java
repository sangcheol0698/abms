package kr.co.abacus.abms.adapter.web.department.dto;

import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.department.dto.DepartmentLeaderInfo;
import kr.co.abacus.abms.application.department.dto.OrganizationChartInfo;

public record OrganizationChartResponse(
    UUID departmentId,
    String departmentName,
    String departmentCode,
    String departmentType,
    @Nullable LeaderResponse departmentLeader,
    int employeeCount,
    List<OrganizationChartResponse> children
) {

    public static OrganizationChartResponse of(OrganizationChartInfo organizationChartInfo) {
        DepartmentLeaderInfo departmentLeaderInfo = organizationChartInfo.leader();

        return new OrganizationChartResponse(
            organizationChartInfo.departmentId(),
            organizationChartInfo.departmentName(),
            organizationChartInfo.departmentCode(),
            organizationChartInfo.departmentType().getDescription(),
            getLeader(departmentLeaderInfo),
            organizationChartInfo.employeeCount(),
            getOrganizationChartResponses(organizationChartInfo)
        );
    }

    private static @Nullable LeaderResponse getLeader(@Nullable DepartmentLeaderInfo departmentLeaderInfo) {
        return departmentLeaderInfo != null && departmentLeaderInfo.employeeId() != null ? LeaderResponse.of(departmentLeaderInfo.employeeId(), departmentLeaderInfo.employeeName(), departmentLeaderInfo.position()) : null;
    }

    private static List<OrganizationChartResponse> getOrganizationChartResponses(OrganizationChartInfo organizationChartInfo) {
        return organizationChartInfo.children().stream()
            .map(OrganizationChartResponse::of)
            .toList();
    }

}
