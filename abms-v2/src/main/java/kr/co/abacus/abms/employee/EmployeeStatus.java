package kr.co.abacus.abms.employee;

import kr.co.abacus.abms.common.domain.Labeled;

public enum EmployeeStatus implements Labeled {

    ACTIVE("재직"),
    ON_LEAVE("휴직"),
    RESIGNED("퇴사");

    private final String label;

    EmployeeStatus(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

}
