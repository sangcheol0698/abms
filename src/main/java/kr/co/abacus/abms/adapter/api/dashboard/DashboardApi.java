package kr.co.abacus.abms.adapter.api.dashboard;

import kr.co.abacus.abms.application.dashboard.dto.DashboardSummaryResponse;
import kr.co.abacus.abms.application.dashboard.inbound.DashboardFinder;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboards")
public class DashboardApi {

    private static final String SYSTEM_DELETER = "SYSTEM";

    private final DashboardFinder dashboardFinder;

    @GetMapping("/summary")
    public DashboardSummaryResponse getDashboardSummary() {
        return dashboardFinder.getDashboardSummary();
    }

}