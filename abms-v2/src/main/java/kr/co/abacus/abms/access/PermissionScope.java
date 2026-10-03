package kr.co.abacus.abms.access;

import kr.co.abacus.abms.common.domain.Labeled;

/**
 * 권한이 미치는 데이터 범위.
 */
public enum PermissionScope implements Labeled {

    ALL("전체"),
    OWN_DEPARTMENT_TREE("본인 부서 + 하위 부서"),
    OWN_DEPARTMENT("본인 부서"),
    CURRENT_PARTICIPATION("현재 참여 프로젝트"),
    SELF("본인");

    private final String label;

    PermissionScope(String label) {
        this.label = label;
    }

    @Override
    public String label() {
        return label;
    }

}
