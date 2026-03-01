package kr.co.abacus.abms.domain.department;

import lombok.Getter;

@Getter
public enum DepartmentType {

    COMPANY("전사"),
    DIVISION("본부"),
    GROUP("담당"),
    TEAM("팀"),
    LAB("연구소"),
    TF("TF");

    private final String description;

    DepartmentType(String description) {
        this.description = description;
    }
}
