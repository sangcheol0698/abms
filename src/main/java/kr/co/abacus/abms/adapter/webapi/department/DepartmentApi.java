package kr.co.abacus.abms.adapter.webapi.department;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.webapi.department.dto.OrganizationChartResponse;
import kr.co.abacus.abms.adapter.webapi.department.dto.OrganizationChartWithEmployeesResponse;
import kr.co.abacus.abms.application.department.dto.OrganizationChartModel;
import kr.co.abacus.abms.application.department.dto.OrganizationChartWithEmployeesModel;
import kr.co.abacus.abms.application.department.provided.DepartmentFinder;

@RequiredArgsConstructor
@RestController
public class DepartmentApi {

    private final DepartmentFinder departmentFinder;

    @GetMapping("/api/departments/organization-chart")
    public OrganizationChartResponse getOrganizationChart() {
        OrganizationChartModel organizationChartModel = departmentFinder.getOrganizationChart();

        return OrganizationChartResponse.of(organizationChartModel);
    }

    @GetMapping("/api/departments/organization-chart/employees")
    public OrganizationChartWithEmployeesResponse getOrganizationChartWithEmployee() {
        OrganizationChartWithEmployeesModel organizationChartWithEmployee = departmentFinder.getOrganizationChartWithEmployees();

        return OrganizationChartWithEmployeesResponse.of(organizationChartWithEmployee);
    }

}
