package kr.co.abacus.abms.security;

/**
 * 권한 범위로 접근을 판단할 프로젝트. 프로젝트 엔티티가 구현해 {@link AccessService} 가 프로젝트 패키지를 직접 알지 않게 한다.
 */
public interface ScopedProject {

    Long id();

    /** 손익이 귀속되는 주관 부서 */
    Long getLeadDepartmentId();

}
