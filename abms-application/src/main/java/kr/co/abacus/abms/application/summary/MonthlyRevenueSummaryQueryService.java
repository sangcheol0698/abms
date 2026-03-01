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

    @Transactional(readOnly = true)
    @Override
    public List<MonthlyRevenueSummary> findRecentSixMonths(String yearMonthStr) {
        List<MonthlyRevenueSummary> resultList = new ArrayList<>();

        // 1. 기준달 파싱 (예: "202602")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        YearMonth targetMonth = YearMonth.parse(yearMonthStr, formatter);

        // 2. 5개월 전부터 ~ 이번 달까지 (총 6회 반복)
        // todo: one-query 로 수정 필요
        // todo: summary_date 인덱스 필요
        for (int i = 5; i >= 0; i--) {
            YearMonth currentMonth = targetMonth.minusMonths(i);

            // 3. 해당 월의 1일 ~ 말일 구하기
            LocalDate startOfMonth = currentMonth.atDay(1);
            LocalDate endOfMonth = currentMonth.atEndOfMonth();

            // 4. 해당 월의 최신 집계 데이터 1건 조회
            // 데이터가 있으면 리스트에 추가, 없으면(Optional.empty) 넘어감
            monthlyRevenueSummaryRepository.findFirstBySummaryDateBetweenOrderBySummaryDateDesc(startOfMonth, endOfMonth)
                .ifPresent(summary -> resultList.add(summary));
        }

        return resultList;
    }

}
