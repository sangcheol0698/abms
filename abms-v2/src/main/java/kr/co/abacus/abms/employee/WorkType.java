package kr.co.abacus.abms.employee;

import kr.co.abacus.abms.common.domain.Labeled;

/**
 * 근무 형태.
 */
public enum WorkType implements Labeled {

    OFFICE("사내 근무"),
    CLIENT_SITE("고객사 상주"),
    REMOTE("원격"),
    HYBRID("혼합");

    private final String label;

    WorkType(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

}
