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
        List<OrganizationChartResponse> children,
        int employeeCount
) {

    public static OrganizationChartResponse of(OrganizationChartDetail organizationChartDetail) {
        DepartmentLeaderDetail departmentLeaderDetail = organizationChartDetail.leader();

        return new OrganizationChartResponse(
                organizationChartDetail.departmentId(),
                organizationChartDetail.departmentName(),
                organizationChartDetail.departmentCode(),
                organizationChartDetail.departmentType().getDescription(),
                getLeader(departmentLeaderDetail),
                getOrganizationChartResponses(organizationChartDetail),
                organizationChartDetail.employeeCount()
        );
    }

    private static @Nullable LeaderResponse getLeader(@Nullable DepartmentLeaderDetail departmentLeaderDetail) {
        return departmentLeaderDetail != null ? LeaderResponse.of(
                departmentLeaderDetail.leaderEmployeeId(), departmentLeaderDetail.leaderEmployeeName(),
                departmentLeaderDetail.leaderPosition()) : null;
    }

    private static List<OrganizationChartResponse> getOrganizationChartResponses(
            OrganizationChartDetail organizationChartDetail
    ) {
        return organizationChartDetail.children().stream()
                .map(OrganizationChartResponse::of)
                .toList();
    }

}
