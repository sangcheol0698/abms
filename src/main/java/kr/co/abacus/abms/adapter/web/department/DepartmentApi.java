package kr.co.abacus.abms.adapter.web.department;

import java.util.List;

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
import kr.co.abacus.abms.adapter.web.department.dto.DepartmentAssignLeaderResponse;
import kr.co.abacus.abms.adapter.web.department.dto.DepartmentDetailResponse;
import kr.co.abacus.abms.adapter.web.department.dto.EmployeeAssignLeaderRequest;
import kr.co.abacus.abms.adapter.web.department.dto.OrganizationChartResponse;
import kr.co.abacus.abms.adapter.web.employee.dto.EmployeeSearchResponse;
import kr.co.abacus.abms.application.department.dto.DepartmentDetail;
import kr.co.abacus.abms.application.department.dto.OrganizationChartDetail;
import kr.co.abacus.abms.application.department.inbound.DepartmentFinder;
import kr.co.abacus.abms.application.department.inbound.DepartmentManager;
import kr.co.abacus.abms.application.employee.dto.EmployeeSummary;

@RequiredArgsConstructor
@RestController
public class DepartmentApi {

    private final DepartmentFinder departmentFinder;
    private final DepartmentManager departmentManager;

    @GetMapping("/api/departments/organization-chart")
    public List<OrganizationChartResponse> getOrganizationChart() {
        List<OrganizationChartDetail> organizationChartDetails = departmentFinder.getOrganizationChart();

        return organizationChartDetails.stream()
                .map(OrganizationChartResponse::of)
                .toList();
    }

    @GetMapping("/api/departments/{departmentId}")
    public DepartmentDetailResponse getDepartment(@PathVariable Long departmentId) {
        DepartmentDetail detail = departmentFinder.findDetail(departmentId);

        return DepartmentDetailResponse.of(detail);
    }

    @GetMapping("/api/departments/{departmentId}/employees")
    public PageResponse<EmployeeSearchResponse> getDepartmentEmployees(
            @PathVariable Long departmentId,
            @RequestParam(required = false) String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<EmployeeSummary> employeesPage = departmentFinder.getEmployees(departmentId, name, pageable);

        Page<EmployeeSearchResponse> responses = employeesPage.map(EmployeeSearchResponse::of);

        return PageResponse.of(responses);
    }

    @PostMapping("/api/departments/{departmentId}/assign-team-leader")
    public DepartmentAssignLeaderResponse assignTeamLeader(@PathVariable Long departmentId,
                                                           @Valid @RequestBody EmployeeAssignLeaderRequest request) {
        Long id = departmentManager.assignLeader(departmentId, request.leaderEmployeeId());

        return DepartmentAssignLeaderResponse.of(id);
    }

}
