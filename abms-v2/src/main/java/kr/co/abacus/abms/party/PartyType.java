package kr.co.abacus.abms.party;

import kr.co.abacus.abms.common.domain.Labeled;

/**
 * 거래처 구분. 고객사(발주처)와 협력사(외주·파트너)를 함께 관리한다.
 */
public enum PartyType implements Labeled {

    CLIENT("고객사"),
    PARTNER("협력사"),
    BOTH("고객사·협력사");

    private final String label;

    PartyType(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

}
