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

}
