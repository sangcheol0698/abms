package kr.co.abacus.abms.application.summary.inbound;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummaryTotal;

public interface MonthlyRevenueSummaryFinder {

    Optional<MonthlyRevenueSummaryTotal> findOptionalByTargetMonth(String yearMonthStr);

    MonthlyRevenueSummaryTotal findByTargetMonth(String yearMonthStr);

    List<MonthlyRevenueSummaryTotal> findRecentSixMonths(String yearMonthStr);
}
