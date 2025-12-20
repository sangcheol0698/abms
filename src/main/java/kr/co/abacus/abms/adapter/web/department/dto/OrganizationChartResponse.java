package kr.co.abacus.abms.adapter.web.department.dto;

import java.util.List;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.department.dto.DepartmentLeaderDetail;
import kr.co.abacus.abms.application.department.dto.OrganizationChartDetail;

public record OrganizationChartResponse(
    Long departmentId,
    String departmentName,
    String departmentCode,
    String departmentType,
    @Nullable LeaderResponse departmentLeader,
    int employeeCount,
    List<OrganizationChartResponse> children) {

    public static OrganizationChartResponse of(OrganizationChartDetail organizationChartDetail) {
        DepartmentLeaderDetail departmentLeaderDetail = organizationChartDetail.leader();

        return new OrganizationChartResponse(
            organizationChartDetail.departmentId(),
            organizationChartDetail.departmentName(),
            organizationChartDetail.departmentCode(),
            organizationChartDetail.departmentType().getDescription(),
            getLeader(departmentLeaderDetail),
            organizationChartDetail.employeeCount(),
            getOrganizationChartResponses(organizationChartDetail));
    }

    private static @Nullable LeaderResponse getLeader(@Nullable DepartmentLeaderDetail departmentLeaderDetail) {
        return departmentLeaderDetail != null ? LeaderResponse.of(
            departmentLeaderDetail.leaderEmployeeId(), departmentLeaderDetail.leaderEmployeeName(),
            departmentLeaderDetail.leaderPosition()) : null;
    }

    private static List<OrganizationChartResponse> getOrganizationChartResponses(
        OrganizationChartDetail organizationChartDetail) {
        return organizationChartDetail.children().stream()
            .map(OrganizationChartResponse::of)
            .toList();
    }

}
