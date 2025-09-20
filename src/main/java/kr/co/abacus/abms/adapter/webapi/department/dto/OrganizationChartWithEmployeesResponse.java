package kr.co.abacus.abms.adapter.webapi.department.dto;

import java.util.List;
import java.util.UUID;

import kr.co.abacus.abms.application.department.dto.EmployeeModel;
import kr.co.abacus.abms.application.department.dto.LeaderModel;
import kr.co.abacus.abms.application.department.dto.OrganizationChartWithEmployeesModel;

public record OrganizationChartWithEmployeesResponse(
    UUID departmentId,
    String departmentName,
    String departmentCode,
    String departmentType,
    LeaderResponse departmentLeader,
    List<OrganizationEmployeeResponse> employees,
    List<OrganizationChartWithEmployeesResponse> children
) {

    public static OrganizationChartWithEmployeesResponse of(OrganizationChartWithEmployeesModel organizationChartWithEmployeesModel) {
        LeaderModel leaderModel = organizationChartWithEmployeesModel.leader();
        List<EmployeeModel> employeeModels = organizationChartWithEmployeesModel.employeeModels();
        List<OrganizationChartWithEmployeesModel> children = organizationChartWithEmployeesModel.children();

        return new OrganizationChartWithEmployeesResponse(
            organizationChartWithEmployeesModel.departmentId(),
            organizationChartWithEmployeesModel.departmentName(),
            organizationChartWithEmployeesModel.departmentCode(),
            organizationChartWithEmployeesModel.departmentType().getDescription(),
            leaderModel != null ? LeaderResponse.of(leaderModel.employeeId(), leaderModel.employeeName(), leaderModel.position()) : null,
            getOrganizationEmployeeResponse(employeeModels),
            getOrganizationChartWithEmployeesResponses(children)
        );
    }

    private static List<OrganizationEmployeeResponse> getOrganizationEmployeeResponse(List<EmployeeModel> employeeModels) {
        return employeeModels.stream().map(employeeModel ->
                OrganizationEmployeeResponse.of(employeeModel.employeeId(), employeeModel.employeeName(), employeeModel.position().getDescription()))
            .toList();
    }

    private static List<OrganizationChartWithEmployeesResponse> getOrganizationChartWithEmployeesResponses(List<OrganizationChartWithEmployeesModel> children) {
        return children.stream().map(OrganizationChartWithEmployeesResponse::of).toList();
    }

}
