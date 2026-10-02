package kr.co.abacus.abms.access;

import kr.co.abacus.abms.common.domain.Labeled;

public enum PermissionGroupType implements Labeled {

    SYSTEM("시스템"),
    CUSTOM("사용자 정의");

    private final String label;

    PermissionGroupType(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

}
