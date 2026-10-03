package kr.co.abacus.abms.department;

import kr.co.abacus.abms.common.domain.Labeled;

public enum DepartmentType implements Labeled {

    COMPANY("전사"),
    DIVISION("본부"),
    GROUP("담당"),
    TEAM("팀"),
    LAB("연구소"),
    TF("TF");

    private final String label;

    DepartmentType(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

}
