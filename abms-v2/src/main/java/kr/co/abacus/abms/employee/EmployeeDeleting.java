package kr.co.abacus.abms.employee;

/**
 * 직원 삭제 직전에 같은 트랜잭션 안에서 발행한다.
 * 직원을 참조하는 쪽(프로젝트 투입)이 받아서, 삭제할 수 없으면 {@link kr.co.abacus.abms.common.domain.BusinessException} 을 던진다.
 */
public record EmployeeDeleting(Long employeeId) {
}
