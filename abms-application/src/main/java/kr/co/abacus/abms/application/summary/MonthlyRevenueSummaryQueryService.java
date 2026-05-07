package kr.co.abacus.abms.application.summary;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.application.summary.inbound.MonthlyRevenueSummaryFinder;
import kr.co.abacus.abms.application.summary.outbound.MonthlyRevenueSummaryRepository;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummaryTotal;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlyRevenueSummaryQueryService implements MonthlyRevenueSummaryFinder {

    private final MonthlyRevenueSummaryRepository monthlyRevenueSummaryRepository;

    @Override
    public Optional<MonthlyRevenueSummaryTotal> findOptionalByTargetMonth(String yearMonthStr) {
        YearMonth yearMonth = parseYearMonth(yearMonthStr);
        LocalDate targetMonth = yearMonth.atDay(1);
        List<MonthlyRevenueSummary> summaries = monthlyRevenueSummaryRepository
                .findAllByTargetMonthAndDeletedFalseOrderByProjectIdAsc(targetMonth);

        if (summaries.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(toTotal(targetMonth, summaries));
    }

    @Override
    public MonthlyRevenueSummaryTotal findByTargetMonth(String yearMonthStr) {
        return findOptionalByTargetMonth(yearMonthStr)
                .orElseThrow(() -> new IllegalArgumentException("계산되지 않은 월별 집계: " + yearMonthStr));
    }

    @Transactional(readOnly = true)
    @Override
    public List<MonthlyRevenueSummaryTotal> findRecentSixMonths(String yearMonthStr) {
        YearMonth targetMonth = parseYearMonth(yearMonthStr);
        LocalDate startMonth = targetMonth.minusMonths(5).atDay(1);
        LocalDate endMonth = targetMonth.atDay(1);
        List<MonthlyRevenueSummary> summaries = monthlyRevenueSummaryRepository
                .findAllByTargetMonthBetweenAndDeletedFalseOrderByTargetMonthAscProjectIdAsc(startMonth, endMonth);

        List<MonthlyRevenueSummaryTotal> totals = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate currentMonth = targetMonth.minusMonths(i).atDay(1);
            List<MonthlyRevenueSummary> monthlySummaries = summaries.stream()
                    .filter(summary -> summary.getTargetMonth().isEqual(currentMonth))
                    .toList();
            if (!monthlySummaries.isEmpty()) {
                totals.add(toTotal(currentMonth, monthlySummaries));
            }
        }

        return totals;
    }

    private YearMonth parseYearMonth(String yearMonthStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        return YearMonth.parse(yearMonthStr, formatter);
    }

    private MonthlyRevenueSummaryTotal toTotal(LocalDate targetMonth, List<MonthlyRevenueSummary> summaries) {
        Money revenue = summaries.stream()
                .map(MonthlyRevenueSummary::getRevenueAmount)
                .reduce(Money.zero(), Money::add);
        Money cost = summaries.stream()
                .map(MonthlyRevenueSummary::getCostAmount)
                .reduce(Money.zero(), Money::add);
        Money profit = summaries.stream()
                .map(MonthlyRevenueSummary::getProfitAmount)
                .reduce(Money.zero(), Money::add);

        return new MonthlyRevenueSummaryTotal(targetMonth, revenue, cost, profit);
    }

}
