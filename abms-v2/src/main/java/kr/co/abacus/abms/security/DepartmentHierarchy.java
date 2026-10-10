package kr.co.abacus.abms.security;

import java.util.Set;

/**
 * 부서 트리 조회. "소속 부서 및 하위 부서" 범위를 해석할 때 쓴다. 부서 리포지토리가 구현한다.
 */
public interface DepartmentHierarchy {

    /** 해당 부서와 모든 하위 부서의 id */
    Set<Long> subtreeIds(Long departmentId);

}
