package kr.co.abacus.abms.application.dashboard.inbound;

import java.time.Year;
import java.util.List;

import kr.co.abacus.abms.application.dashboard.dto.DashboardDepartmentFinancialItem;
import kr.co.abacus.abms.application.dashboard.dto.DashboardEmployeeOverviewResponse;
import kr.co.abacus.abms.application.dashboard.dto.DashboardMonthlyFinancialItem;
import kr.co.abacus.abms.application.dashboard.dto.DashboardProjectOverviewResponse;
import kr.co.abacus.abms.application.dashboard.dto.DashboardSummaryResponse;
import kr.co.abacus.abms.application.dashboard.dto.DashboardUpcomingDeadlineItem;

/**
 * 대시보드 집계 조회
 */
public interface DashboardFinder {

    default DashboardSummaryResponse getDashboardSummary() {
        return getDashboardSummary(Year.now().getValue());
    }

    DashboardSummaryResponse getDashboardSummary(int year);

    List<DashboardMonthlyFinancialItem> getMonthlyFinancials(int year);

    List<DashboardUpcomingDeadlineItem> getUpcomingDeadlines(int limit);

    List<DashboardDepartmentFinancialItem> getDepartmentFinancials(int year, int limit);

    DashboardProjectOverviewResponse getProjectOverview(int year);

    DashboardEmployeeOverviewResponse getEmployeeOverview(int year);

}
