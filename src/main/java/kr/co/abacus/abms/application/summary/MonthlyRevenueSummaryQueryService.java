package kr.co.abacus.abms.application.summary;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.application.summary.inbound.MonthlyRevenueSummaryFinder;
import kr.co.abacus.abms.application.summary.outbound.MonthlyRevenueSummaryRepository;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlyRevenueSummaryQueryService implements MonthlyRevenueSummaryFinder {

    private final MonthlyRevenueSummaryRepository monthlyRevenueSummaryRepository;

    @Override
    public MonthlyRevenueSummary findByTargetMonth(String yearMonthStr) {
        // 1. 문자열 파싱 ("202602" -> 2026년 2월)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        YearMonth yearMonth = YearMonth.parse(yearMonthStr, formatter);

        // 2. 검색 기간 설정 (2월 1일 ~ 2월 28일)
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        MonthlyRevenueSummary summary = monthlyRevenueSummaryRepository.findFirstBySummaryDateBetweenOrderBySummaryDateDesc(startOfMonth, endOfMonth)
            .orElseThrow(() -> new IllegalArgumentException("계산되지 않은 월별 집계: " + yearMonthStr));

        return summary;
    }

}
