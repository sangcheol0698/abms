package kr.co.abacus.abms.project;

import kr.co.abacus.abms.common.domain.Labeled;

public enum ProjectStatus implements Labeled {

    SCHEDULED("예약"),
    IN_PROGRESS("진행 중"),
    COMPLETED("완료"),
    ON_HOLD("보류"),
    CANCELLED("취소");

    private final String label;

    ProjectStatus(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

    public boolean isClosed() {
        return this == COMPLETED || this == CANCELLED;
    }

    /**
     * 진행 기준(관리) 매출을 기간대로 배분하는지. 취소·보류된 프로젝트는 계약금액을 기간대로 받는다고 볼 수 없으므로
     * 실제 청구액을 관리 매출로 본다.
     */
    public boolean accruesManagedRevenue() {
        return this != CANCELLED && this != ON_HOLD;
    }

}
