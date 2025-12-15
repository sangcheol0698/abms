package kr.co.abacus.abms.adapter.web.department;

import java.util.List;
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

import kr.co.abacus.abms.adapter.web.PageResponse;
import kr.co.abacus.abms.adapter.web.department.dto.EmployeeAssignTeamLeaderRequest;
import kr.co.abacus.abms.adapter.web.department.dto.OrganizationChartResponse;
import kr.co.abacus.abms.adapter.web.employee.dto.EmployeeSearchResponse;
import kr.co.abacus.abms.application.department.dto.OrganizationChartInfo;
import kr.co.abacus.abms.application.department.inbound.DepartmentFinder;
import kr.co.abacus.abms.application.department.inbound.DepartmentManager;
import kr.co.abacus.abms.application.employee.dto.EmployeeSummary;
import kr.co.abacus.abms.domain.department.Department;

@RequiredArgsConstructor
@RestController
public class DepartmentApi {

    private final DepartmentFinder departmentFinder;
    private final DepartmentManager departmentManager;

    @GetMapping("/api/departments/organization-chart")
    public List<OrganizationChartResponse> getOrganizationChart() {
        List<OrganizationChartInfo> organizationChartInfos = departmentFinder.getOrganizationChart();

        return organizationChartInfos.stream()
            .map(OrganizationChartResponse::of)
            .toList();
    }

    @GetMapping("/api/departments/{departmentId}")
    public DepartmentResponse getDepartment(@PathVariable UUID departmentId) {
        Department department = departmentFinder.find(departmentId);

        return DepartmentResponse.of(department);
    }

    @GetMapping("/api/departments/{departmentId}/employees")
    public PageResponse<EmployeeSearchResponse> getDepartmentEmployees(
        @PathVariable UUID departmentId,
        @RequestParam(required = false) String name,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<EmployeeSummary> employeesPage = departmentFinder.getEmployees(departmentId, name, pageable);

        Page<EmployeeSearchResponse> responses = employeesPage.map(EmployeeSearchResponse::of);

        return PageResponse.of(responses);
    }

    @PostMapping("/api/departments/{departmentId}/assign-team-leader")
    public DepartmentResponse assignTeamLeader(@PathVariable UUID departmentId, @Valid @RequestBody EmployeeAssignTeamLeaderRequest request) {
        Department department = departmentManager.assignTeamLeader(departmentId, request.leaderEmployeeId());

        return DepartmentResponse.of(department);
    }

}
