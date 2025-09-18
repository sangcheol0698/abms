package kr.co.abacus.abms.adapter.webapi.department.dto;

import java.util.List;
import java.util.UUID;

import kr.co.abacus.abms.application.department.dto.LeaderModel;
import kr.co.abacus.abms.application.department.dto.OrganizationChartModel;

public record OrganizationChartResponse(
    UUID departmentId,
    String departmentName,
    String departmentCode,
    String departmentType,
    LeaderResponse leader,
    List<OrganizationChartResponse> children
) {

    public static OrganizationChartResponse of(OrganizationChartModel organizationChartModel) {
        LeaderModel leaderModel = organizationChartModel.leader();

        return new OrganizationChartResponse(
            organizationChartModel.departmentId(),
            organizationChartModel.departmentName(),
            organizationChartModel.departmentCode(),
            organizationChartModel.departmentType().getDescription(),
            getLeader(leaderModel),
            getOrganizationChartResponses(organizationChartModel)
        );
    }

    private static LeaderResponse getLeader(LeaderModel leaderModel) {
        return leaderModel != null ? LeaderResponse.of(leaderModel.employeeId(), leaderModel.employeeName(), leaderModel.position()) : null;
    }

    private static List<OrganizationChartResponse> getOrganizationChartResponses(OrganizationChartModel organizationChartModel) {
        return organizationChartModel.children().stream()
            .map(OrganizationChartResponse::of)
            .toList();
    }

}
