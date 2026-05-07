package kr.co.abacus.abms.application.summary;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.application.summary.inbound.CompanyMonthlyCostSummaryFinder;
import kr.co.abacus.abms.application.summary.outbound.CompanyMonthlyCostSummaryRepository;
import kr.co.abacus.abms.application.summary.outbound.MonthlyRevenueSummaryRepository;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.summary.CompanyMonthlyCostSummary;
import kr.co.abacus.abms.domain.summary.CompanyMonthlyCostSummaryTotal;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyMonthlyCostSummaryQueryService implements CompanyMonthlyCostSummaryFinder {

    private final CompanyMonthlyCostSummaryRepository companyMonthlyCostSummaryRepository;
    private final MonthlyRevenueSummaryRepository monthlyRevenueSummaryRepository;

    @Override
    public CompanyMonthlyCostSummaryTotal findByTargetMonth(String yearMonthStr) {
        LocalDate targetMonth = parseYearMonth(yearMonthStr).atDay(1);
        CompanyMonthlyCostSummary costSummary = companyMonthlyCostSummaryRepository
                .findByTargetMonthAndDeletedFalse(targetMonth)
                .orElseThrow(() -> new IllegalArgumentException("계산되지 않은 전사 월 비용 집계: " + yearMonthStr));
        List<MonthlyRevenueSummary> revenueSummaries = monthlyRevenueSummaryRepository
                .findAllByTargetMonthAndDeletedFalseOrderByProjectIdAsc(targetMonth);

        return toTotal(costSummary, revenueSummaries);
    }

    @Override
    public List<CompanyMonthlyCostSummaryTotal> findRecentSixMonths(String yearMonthStr) {
        YearMonth targetYearMonth = parseYearMonth(yearMonthStr);
        LocalDate startMonth = targetYearMonth.minusMonths(5).atDay(1);
        LocalDate endMonth = targetYearMonth.atDay(1);
        List<CompanyMonthlyCostSummary> costSummaries = companyMonthlyCostSummaryRepository
                .findAllByTargetMonthBetweenAndDeletedFalseOrderByTargetMonthAsc(startMonth, endMonth);
        List<MonthlyRevenueSummary> revenueSummaries = monthlyRevenueSummaryRepository
                .findAllByTargetMonthBetweenAndDeletedFalseOrderByTargetMonthAscProjectIdAsc(startMonth, endMonth);

        List<CompanyMonthlyCostSummaryTotal> totals = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate currentMonth = targetYearMonth.minusMonths(i).atDay(1);
            costSummaries.stream()
                    .filter(summary -> summary.getTargetMonth().isEqual(currentMonth))
                    .findFirst()
                    .map(summary -> toTotal(summary, revenueSummaries.stream()
                            .filter(revenueSummary -> revenueSummary.getTargetMonth().isEqual(currentMonth))
                            .toList()))
                    .ifPresent(totals::add);
        }

        return totals;
    }

    private YearMonth parseYearMonth(String yearMonthStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        return YearMonth.parse(yearMonthStr, formatter);
    }

    private CompanyMonthlyCostSummaryTotal toTotal(
            CompanyMonthlyCostSummary costSummary,
            List<MonthlyRevenueSummary> revenueSummaries
    ) {
        Money revenue = revenueSummaries.stream()
                .map(MonthlyRevenueSummary::getRevenueAmount)
                .reduce(Money.zero(), Money::add);
        Money projectAllocatedCost = revenueSummaries.stream()
                .map(MonthlyRevenueSummary::getCostAmount)
                .reduce(Money.zero(), Money::add);
        Money unallocatedCost = costSummary.getUnallocatedFullTimeEmployeeCost();
        Money cost = projectAllocatedCost.add(unallocatedCost);
        Money profit = revenue.subtract(cost);

        return new CompanyMonthlyCostSummaryTotal(
                costSummary.getTargetMonth(),
                revenue,
                projectAllocatedCost,
                unallocatedCost,
                cost,
                profit
        );
    }
}
