package kr.co.abacus.abms.party;

import kr.co.abacus.abms.common.domain.Labeled;

public enum ContactRole implements Labeled {

    SALES("영업"),
    CONTRACT("계약"),
    BILLING("청구·정산"),
    TECH("기술"),
    ETC("기타");

    private final String label;

    ContactRole(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

}
