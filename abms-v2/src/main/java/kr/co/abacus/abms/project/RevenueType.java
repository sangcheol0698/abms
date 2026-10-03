package kr.co.abacus.abms.project;

import kr.co.abacus.abms.common.domain.Labeled;

public enum RevenueType implements Labeled {

    DOWN_PAYMENT("착수금"),
    INTERMEDIATE_PAYMENT("중도금"),
    BALANCE_PAYMENT("잔금"),
    MAINTENANCE("유지보수"),
    ETC("기타");

    private final String label;

    RevenueType(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

}
