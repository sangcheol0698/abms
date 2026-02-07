package kr.co.abacus.abms.application.dashboard.inbound;

import kr.co.abacus.abms.application.dashboard.dto.DashboardSummaryResponse;

/**
 * 대시보드 집계 조회
 */
public interface DashboardFinder {
    DashboardSummaryResponse getDashboardSummary();
}