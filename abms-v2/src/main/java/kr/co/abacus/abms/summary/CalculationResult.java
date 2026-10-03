package kr.co.abacus.abms.summary;

import java.time.YearMonth;
import java.util.List;

import kr.co.abacus.abms.common.domain.Money;

/**
 * 월 손익 재집계 결과.
 *
 * @param skipped  마감 등으로 집계를 건너뛰었는지
 * @param warnings 급여/정책 누락 등 확인이 필요한 항목
 */
public record CalculationResult(
        YearMonth month,
        boolean skipped,
        int employeeCostCount,
        int projectCount,
        int removedCount,
        Money revenue,
        Money cost,
        List<String> warnings
) implements java.io.Serializable {

    public static CalculationResult skipped(YearMonth month, String reason) {
        return new CalculationResult(month, true, 0, 0, 0, Money.ZERO, Money.ZERO, List.of(reason));
    }

    public Money profit() {
        return revenue.minus(cost);
    }

}
