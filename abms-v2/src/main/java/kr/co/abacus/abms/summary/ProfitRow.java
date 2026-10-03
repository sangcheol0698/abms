package kr.co.abacus.abms.summary;

import java.math.BigDecimal;
import java.math.RoundingMode;

import kr.co.abacus.abms.common.domain.Money;

/**
 * 손익 한 줄 (프로젝트/부서/월 단위 공용).
 */
public record ProfitRow(Long id, String code, String name, Money revenue, Money cost) {

    public Money profit() {
        return revenue.minus(cost);
    }

    /** 이익률(%) — 매출이 없으면 null 대신 0 */
    public BigDecimal margin() {
        return margin(revenue, cost);
    }

    public static BigDecimal margin(Money revenue, Money cost) {
        if (revenue.amount().signum() == 0) {
            return BigDecimal.ZERO;
        }
        return revenue.minus(cost).amount().multiply(BigDecimal.valueOf(100))
                .divide(revenue.amount(), 1, RoundingMode.HALF_UP);
    }

    public ProfitRow plus(Money revenue, Money cost) {
        return new ProfitRow(id, code, name, this.revenue.plus(revenue), this.cost.plus(cost));
    }

}
