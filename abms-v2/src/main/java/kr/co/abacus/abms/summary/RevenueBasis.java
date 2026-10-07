package kr.co.abacus.abms.summary;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.domain.Labeled;
import kr.co.abacus.abms.common.domain.Money;

/**
 * 손익을 볼 때의 매출 인식 기준.
 */
public enum RevenueBasis implements Labeled {

    /** 세금계산서가 발행된 청구 계획의 청구일 기준 (회계 매출) */
    BILLING("청구 기준", "세금계산서를 발행한 달에 매출을 잡습니다. 회계 장부와 같은 숫자입니다."),
    /** 계약금액을 프로젝트 기간에 일할 배분 (관리 매출) */
    MANAGED("진행 기준", "계약금액을 프로젝트 기간에 날짜 비율로 나눠 매달 매출을 잡습니다. 그달 일한 만큼의 성과입니다. (취소·보류 프로젝트는 청구한 금액)");

    private final String label;
    private final String description;

    RevenueBasis(String label, String description) {
        this.label = label;
        this.description = description;
    }

    /** 화면에 보여주는 한 줄 설명 */
    public String description() {
        return description;
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
