package kr.co.abacus.abms.application.summary.inbound;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;

public interface MonthlyRevenueSummaryFinder {

    MonthlyRevenueSummary findByTargetMonth(String yearMonthStr);

    List<MonthlyRevenueSummary> findRecentSixMonths(String yearMonthStr);
}
