package kr.co.abacus.abms.adapter.api.department;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.api.common.PageResponse;
import kr.co.abacus.abms.adapter.security.CurrentActorResolver;
import kr.co.abacus.abms.adapter.api.department.dto.DepartmentAssignLeaderResponse;
import kr.co.abacus.abms.adapter.api.department.dto.DepartmentDetailResponse;
import kr.co.abacus.abms.adapter.api.department.dto.EmployeeAssignLeaderRequest;
import kr.co.abacus.abms.adapter.api.department.dto.OrganizationChartResponse;
import kr.co.abacus.abms.adapter.api.employee.dto.EmployeeSearchResponse;
import kr.co.abacus.abms.adapter.api.summary.dto.MonthlyRevenueSummaryResponse;
import kr.co.abacus.abms.application.department.dto.DepartmentDetail;
import kr.co.abacus.abms.application.department.dto.DepartmentRevenueSummary;
import kr.co.abacus.abms.application.department.dto.OrganizationChartDetail;
import kr.co.abacus.abms.application.department.inbound.DepartmentFinder;
import kr.co.abacus.abms.application.department.inbound.DepartmentManager;
import kr.co.abacus.abms.application.employee.dto.EmployeeSummary;

@RequiredArgsConstructor
@RestController
public class DepartmentApi {

    private final DepartmentFinder departmentFinder;
    private final DepartmentManager departmentManager;
    private final CurrentActorResolver currentActorResolver;

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

    @GetMapping("/api/departments/{departmentId}/revenue/sixMonthTrend")
    public List<MonthlyRevenueSummaryResponse> getDepartmentRevenueTrend(
            @PathVariable Long departmentId,
            @RequestParam("yearMonth") String yearMonth) {
        List<DepartmentRevenueSummary> summaries = departmentFinder.getRevenueTrend(departmentId, yearMonth);

        return summaries.stream()
                .map(summary -> new MonthlyRevenueSummaryResponse(
                        summary.targetMonth(),
                        summary.revenue().amount(),
                        summary.cost().amount(),
                        summary.profit().amount()
                ))
                .toList();
    }

    @PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'employee.write')")
    @PostMapping("/api/departments/{departmentId}/assign-team-leader")
    public DepartmentAssignLeaderResponse assignTeamLeader(@PathVariable Long departmentId,
                                                           @Valid @RequestBody EmployeeAssignLeaderRequest request,
                                                           Authentication authentication) {
        Long id = departmentManager.assignLeader(
                currentActorResolver.resolve(authentication),
                departmentId,
                request.leaderEmployeeId()
        );

        return DepartmentAssignLeaderResponse.of(id);
    }
}
