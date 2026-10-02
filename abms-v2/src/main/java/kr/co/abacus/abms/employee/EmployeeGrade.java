package kr.co.abacus.abms.employee;

import kr.co.abacus.abms.common.domain.Labeled;

public enum EmployeeGrade implements Labeled {

    JUNIOR("초급", 1),
    MID_LEVEL("중급", 2),
    SENIOR("고급", 3),
    EXPERT("특급", 4);

    private final String label;
    private final int level;

    EmployeeGrade(String label, int level) {
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
