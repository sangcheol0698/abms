package kr.co.abacus.abms.adapter.api.dashboard;

import java.util.List;

import kr.co.abacus.abms.application.dashboard.dto.DashboardDepartmentFinancialItem;
import kr.co.abacus.abms.application.dashboard.dto.DashboardEmployeeOverviewResponse;
import kr.co.abacus.abms.application.dashboard.dto.DashboardMonthlyFinancialItem;
import kr.co.abacus.abms.application.dashboard.dto.DashboardProjectOverviewResponse;
import kr.co.abacus.abms.application.dashboard.dto.DashboardSummaryResponse;
import kr.co.abacus.abms.application.dashboard.dto.DashboardUpcomingDeadlineItem;
import kr.co.abacus.abms.application.dashboard.inbound.DashboardFinder;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboards")
public class DashboardApi {

    private final DashboardFinder dashboardFinder;

    @GetMapping("/summary")
    public DashboardSummaryResponse getDashboardSummary(
            @RequestParam(required = false) Integer year
    ) {
        return dashboardFinder.getDashboardSummary(year != null ? year : java.time.Year.now().getValue());
    }

    @GetMapping("/monthly-financials")
    public List<DashboardMonthlyFinancialItem> getMonthlyFinancials(
            @RequestParam(required = false) Integer year
    ) {
        return dashboardFinder.getMonthlyFinancials(year != null ? year : java.time.Year.now().getValue());
    }

    @GetMapping("/upcoming-deadlines")
    public List<DashboardUpcomingDeadlineItem> getUpcomingDeadlines(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return dashboardFinder.getUpcomingDeadlines(limit);
    }

    @GetMapping("/department-financials")
    public List<DashboardDepartmentFinancialItem> getDepartmentFinancials(
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return dashboardFinder.getDepartmentFinancials(year != null ? year : java.time.Year.now().getValue(), limit);
    }

    @GetMapping("/project-overview")
    public DashboardProjectOverviewResponse getProjectOverview(
            @RequestParam(required = false) Integer year
    ) {
        return dashboardFinder.getProjectOverview(year != null ? year : java.time.Year.now().getValue());
    }

    @GetMapping("/employee-overview")
    public DashboardEmployeeOverviewResponse getEmployeeOverview(
            @RequestParam(required = false) Integer year
    ) {
        return dashboardFinder.getEmployeeOverview(year != null ? year : java.time.Year.now().getValue());
    }

}
