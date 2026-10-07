package kr.co.abacus.abms.project;

import kr.co.abacus.abms.common.domain.Labeled;

/**
 * 프로젝트 직접비 분류.
 */
public enum ExpenseCategory implements Labeled {

    OUTSOURCING("외주 용역비"),
    EQUIPMENT("장비·자재"),
    LICENSE("소프트웨어·라이선스"),
    CLOUD("클라우드·인프라"),
    TRAVEL("출장·여비"),
    ETC("기타");

    private final String label;

    ExpenseCategory(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

}
