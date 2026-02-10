package kr.co.abacus.abms.domain.employee;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum EmployeePosition {

    // 1. 실무진
    ASSOCIATE("사원", 1),
    SENIOR_ASSOCIATE("선임", 2),
    PRINCIPAL("책임", 3),

    // 2. 중간 관리자
    TEAM_LEADER("팀장", 4),
    CHIEF("수석", 5),

    // 3. 임원진
    DIRECTOR("이사", 6),
    TECHNICAL_DIRECTOR("기술이사", 6),
    MANAGING_DIRECTOR("상무", 6),
    VICE_PRESIDENT("부사장", 7),
    PRESIDENT("사장", 8),
    CHAIRMAN("회장", 9);

    private final String description;
    private final int level;

    EmployeePosition(String description, int level) {
        this.description = description;
        this.level = level;
    }

    public static EmployeePosition fromDescription(String description) {
        return Arrays.stream(values())
                .filter(v -> v.description.equals(description))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 직책: " + description));
    }
}