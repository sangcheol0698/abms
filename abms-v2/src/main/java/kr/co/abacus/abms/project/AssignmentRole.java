package kr.co.abacus.abms.project;

import kr.co.abacus.abms.common.domain.Labeled;

public enum AssignmentRole implements Labeled {

    PM("PM"),
    PL("PL"),
    DEV("개발자"),
    ETC("기타");

    private final String label;

    AssignmentRole(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

}
