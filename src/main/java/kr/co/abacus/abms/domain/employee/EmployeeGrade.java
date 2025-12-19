package kr.co.abacus.abms.domain.employee;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum EmployeeGrade {

    JUNIOR("초급", 1),
    MID_LEVEL("중급", 2),
    SENIOR("고급", 3),
    EXPERT("특급", 4);

    private final String description;
    private final int level;

    EmployeeGrade(String description, int level) {
        this.description = description;
        this.level = level;
    }

    public static EmployeeGrade fromDescription(String description) {
        return Arrays.stream(values())
            .filter(v -> v.description.equals(description))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("알 수 없는 직책: " + description));
    }

    public boolean isHigherThan(EmployeeGrade other) {
        return this.level > other.level;
    }
}