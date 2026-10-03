package kr.co.abacus.abms.employee;

import kr.co.abacus.abms.common.domain.Labeled;

public enum EmployeeType implements Labeled {

    FULL_TIME("정직원"),
    FREELANCER("프리랜서"),
    OUTSOURCING("외주"),
    PART_TIME("반프리");

    private final String label;

    EmployeeType(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

}
