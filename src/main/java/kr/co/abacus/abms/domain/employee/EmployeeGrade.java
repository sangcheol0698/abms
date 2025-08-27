package kr.co.abacus.abms.domain.employee;

import lombok.Getter;

@Getter
public enum EmployeeGrade {

    JUNIOR("초급", 1),
    MID_LEVEL("중급", 2),
    SENIOR("고급", 3),
    EXPERT("특급",  4);

    private final String description;
    private final int level;

    EmployeeGrade(String description, int level) {
        this.description = description;
        this.level = level;
    }

    public boolean isHigherThan(EmployeeGrade other) {
        return this.level > other.level;
    }
}