package kr.co.abacus.abms.domain.grouppermissiongrant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PermissionScope {

    ALL("전체", 1),
    OWN_DEPARTMENT("본인 부서", 2),
    OWN_DEPARTMENT_TREE("본인 부서 및 하위 부서", 3),
    CURRENT_PARTICIPATION("본인 현재 참여", 4),
    SELF("본인", 5);

    private final String description;
    private final int order;
}
