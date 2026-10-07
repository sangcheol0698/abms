package kr.co.abacus.abms.summary;

import java.time.YearMonth;
import java.util.List;

/**
 * 연도 재집계 결과. 월별 결과를 모아 둔다. (마감된 월은 건너뛴 결과로 들어 있다)
 */
public record YearCalculationResult(int year, List<CalculationResult> months) implements java.io.Serializable {

    public List<YearMonth> recalculated() {
        return months.stream().filter(m -> !m.skipped()).map(CalculationResult::month).toList();
    }

    public List<YearMonth> skipped() {
        return months.stream().filter(CalculationResult::skipped).map(CalculationResult::month).toList();
    }

    /** 다시 집계한 월의 확인 필요 항목 (월 표시를 붙인다) */
    public List<String> warnings() {
        return months.stream().filter(m -> !m.skipped())
                .flatMap(m -> m.warnings().stream().map(w -> m.month() + " · " + w))
                .toList();
    }

}
