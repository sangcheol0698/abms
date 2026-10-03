package kr.co.abacus.abms.employee;

import kr.co.abacus.abms.common.domain.Labeled;

public enum EmployeePosition implements Labeled {

    ASSOCIATE("사원", 1),
    SENIOR_ASSOCIATE("선임", 2),
    PRINCIPAL("책임", 3),
    TEAM_LEADER("팀장", 4),
    CHIEF("수석", 5),
    DIRECTOR("이사", 6),
    TECHNICAL_DIRECTOR("기술이사", 6),
    MANAGING_DIRECTOR("상무", 6),
    VICE_PRESIDENT("부사장", 7),
    PRESIDENT("사장", 8),
    CHAIRMAN("회장", 9);

    private final String label;
    private final int level;

    EmployeePosition(String label, int level) {
        this.label = label;
        this.level = level;
    }

    @Override
    public String label() {
        return label;
    }

    public int level() {
        return level;
    }

}
