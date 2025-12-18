package kr.co.abacus.abms.domain.employee;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum EmployeePosition {

    ASSOCIATE("사원", 1),
    STAFF("선임", 2),
    LEADER("책임", 3),
    MANAGER("팀장", 4),
    SENIOR_MANAGER("수석", 5),
    DIRECTOR("이사", 6),
    TECHNICAL_DIRECTOR("기술이사", 6),
    MANAGING_DIRECTOR("상무", 7),
    VICE_PRESIDENT("부사장", 8),
    PRESIDENT("사장", 9);

    private final String description;
    private final int rank;

    EmployeePosition(String description, int rank) {
        this.description = description;
        this.rank = rank;
    }

    public static EmployeePosition fromDescription(String description) {
        return Arrays.stream(values())
            .filter(v -> v.description.equals(description))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("알 수 없는 직책: " + description));
    }
}