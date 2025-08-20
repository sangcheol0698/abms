package kr.co.abacus.abms.domain.employee;

import lombok.Getter;

@Getter
public enum EmployeeGrade {

    JUNIOR("초급"),
    MID_LEVEL("중급"),
    SENIOR("고급"),
    EXPERT("특급");

    private final String description;

    EmployeeGrade(String description) {
        this.description = description;
    }
}