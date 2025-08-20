package kr.co.abacus.abms.domain.employee;

import lombok.Getter;

@Getter
public enum EmployeeType {

    FULL_TIME("정직원"),
    FREELANCE("프리랜서"),
    OUTSOURCING("외주"),
    PART_TIME("반프리");

    private final String description;

    EmployeeType(String description) {
        this.description = description;
    }
}