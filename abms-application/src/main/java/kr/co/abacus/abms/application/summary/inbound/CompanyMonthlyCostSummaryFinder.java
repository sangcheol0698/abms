package kr.co.abacus.abms.application.summary.inbound;

import java.util.List;

import kr.co.abacus.abms.domain.summary.CompanyMonthlyCostSummaryTotal;

public interface CompanyMonthlyCostSummaryFinder {

    CompanyMonthlyCostSummaryTotal findByTargetMonth(String yearMonthStr);

    List<CompanyMonthlyCostSummaryTotal> findRecentSixMonths(String yearMonthStr);
}
