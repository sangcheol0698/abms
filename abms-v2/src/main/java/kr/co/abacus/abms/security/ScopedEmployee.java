package kr.co.abacus.abms.security;

/**
 * 권한 범위로 접근을 판단할 직원. 직원 엔티티가 구현해 {@link AccessService} 가 직원 패키지를 직접 알지 않게 한다.
 */
public interface ScopedEmployee {

    Long id();

    Long getDepartmentId();

}
