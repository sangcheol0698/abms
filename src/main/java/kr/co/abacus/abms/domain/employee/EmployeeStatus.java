package kr.co.abacus.abms.domain.employee;

import lombok.Getter;

@Getter
public enum EmployeeStatus {

    ACTIVE("재직"),
    ON_LEAVE("휴직"),
    RESIGNED("퇴사");

    private final String description;

    EmployeeStatus(String description) {
        this.description = description;
    }
}