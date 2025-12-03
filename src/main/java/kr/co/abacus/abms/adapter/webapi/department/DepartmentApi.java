package kr.co.abacus.abms.adapter.webapi.department;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.webapi.department.dto.DepartmentEmployeesResponse;
import kr.co.abacus.abms.adapter.webapi.department.dto.EmployeeAssignTeamLeaderRequest;
import kr.co.abacus.abms.adapter.webapi.department.dto.OrganizationChartResponse;
import kr.co.abacus.abms.adapter.webapi.department.dto.OrganizationChartWithEmployeesResponse;
import kr.co.abacus.abms.application.department.dto.OrganizationChartModel;
import kr.co.abacus.abms.application.department.dto.OrganizationChartWithEmployeesModel;
import kr.co.abacus.abms.application.department.provided.DepartmentFinder;
import kr.co.abacus.abms.application.department.provided.DepartmentManager;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;

@RequiredArgsConstructor
@RestController
public class DepartmentApi {

    private final DepartmentFinder departmentFinder;
    private final DepartmentManager departmentManager;

    @GetMapping("/api/departments/organization-chart")
    public OrganizationChartResponse getOrganizationChart() {
        OrganizationChartModel organizationChartModel = departmentFinder.getOrganizationChart();

        return OrganizationChartResponse.of(organizationChartModel);
    }

    @GetMapping("/api/departments/organization-chart/employees")
    public OrganizationChartWithEmployeesResponse getOrganizationChartWithEmployee() {
        OrganizationChartWithEmployeesModel organizationChartWithEmployee = departmentFinder
                .getOrganizationChartWithEmployees();

        return OrganizationChartWithEmployeesResponse.of(organizationChartWithEmployee);
    }

    @GetMapping("/api/departments/{departmentId}")
    public DepartmentResponse getDepartment(@PathVariable UUID departmentId) {
        Department department = departmentFinder.find(departmentId);

        return DepartmentResponse.of(department);
    }

    @GetMapping("/api/departments/{departmentId}/employees")
    public DepartmentEmployeesResponse getDepartmentEmployees(
            @PathVariable UUID departmentId,
            @RequestParam(required = false) String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<Employee> employeesPage = departmentFinder.getEmployees(departmentId, name, pageable);

        return DepartmentEmployeesResponse.from(employeesPage);
    }

    @PostMapping("/api/departments/{departmentId}/assign-team-leader")
    public DepartmentResponse assignTeamLeader(@PathVariable UUID departmentId, @Valid @RequestBody EmployeeAssignTeamLeaderRequest request) {
        Department department = departmentManager.assignTeamLeader(departmentId, request.leaderEmployeeId());

        return DepartmentResponse.of(department);
    }
}
