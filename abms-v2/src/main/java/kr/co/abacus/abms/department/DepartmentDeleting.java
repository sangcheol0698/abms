package kr.co.abacus.abms.department;

/**
 * 부서 삭제 직전에 같은 트랜잭션 안에서 발행한다.
 * 부서를 참조하는 쪽(직원, 프로젝트)이 받아서, 삭제할 수 없으면 {@link kr.co.abacus.abms.common.domain.BusinessException} 을 던진다.
 * 부서가 자신을 참조하는 상위 기능을 직접 알지 않도록 둔 경계다.
 */
public record DepartmentDeleting(Long departmentId) {
}
