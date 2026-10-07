package kr.co.abacus.abms.summary;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.domain.Labeled;
import kr.co.abacus.abms.common.domain.Money;

/**
 * 손익을 볼 때의 매출 인식 기준.
 */
public enum RevenueBasis implements Labeled {

    /** 세금계산서가 발행된 청구 계획의 청구일 기준 (회계 매출) */
    BILLING("청구 기준"),
    /** 계약금액을 프로젝트 기간에 일할 배분 (관리 매출) */
    MANAGED("진행 기준");

    private final String label;

    RevenueBasis(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

    public Money revenueOf(MonthlyRevenueSummary summary) {
        return this == MANAGED ? summary.getManagedRevenueAmount() : summary.getRevenueAmount();
    }

    /** 알 수 없는 값은 청구 기준으로 본다. */
    public static RevenueBasis parse(@Nullable String value) {
        return "managed".equalsIgnoreCase(value) ? MANAGED : BILLING;
    }

    public String param() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

}
