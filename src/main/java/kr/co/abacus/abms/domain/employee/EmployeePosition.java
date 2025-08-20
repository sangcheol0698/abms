package kr.co.abacus.abms.domain.employee;

import lombok.Getter;

@Getter
public enum EmployeePosition {

    STAFF("사원"),
    SENIOR("선임"),
    LEADER("책임"),
    MANAGER("팀장"),
    SENIOR_MANAGER("수석"),
    DIRECTOR("이사"),
    TECHNICAL_DIRECTOR("기술이사"),
    VICE_PRESIDENT("부사장"),
    PRESIDENT("사장");

    private final String description;

    EmployeePosition(String description) {
        this.description = description;
    }
}