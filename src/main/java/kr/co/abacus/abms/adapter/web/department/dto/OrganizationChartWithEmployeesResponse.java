package kr.co.abacus.abms.adapter.web.department.dto;

import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.department.dto.EmployeeModel;
import kr.co.abacus.abms.application.department.dto.DepartmentLeaderInfo;
import kr.co.abacus.abms.application.department.dto.OrganizationChartWithEmployeesModel;

public record OrganizationChartWithEmployeesResponse(
    UUID departmentId,
    String departmentName,
    String departmentCode,
    String departmentType,
    @Nullable LeaderResponse departmentLeader,
    List<OrganizationEmployeeResponse> employees,
    List<OrganizationChartWithEmployeesResponse> children
) {

    public static OrganizationChartWithEmployeesResponse of(OrganizationChartWithEmployeesModel organizationChartWithEmployeesModel) {
        DepartmentLeaderInfo departmentLeaderInfo = organizationChartWithEmployeesModel.leader();
        List<EmployeeModel> employeeModels = organizationChartWithEmployeesModel.employeeModels();
        List<OrganizationChartWithEmployeesModel> children = organizationChartWithEmployeesModel.children();

        return new OrganizationChartWithEmployeesResponse(
            organizationChartWithEmployeesModel.departmentId(),
            organizationChartWithEmployeesModel.departmentName(),
            organizationChartWithEmployeesModel.departmentCode(),
            organizationChartWithEmployeesModel.departmentType().getDescription(),
            departmentLeaderInfo != null ? LeaderResponse.of(departmentLeaderInfo.employeeId(), departmentLeaderInfo.employeeName(), departmentLeaderInfo.position()) : null,
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
