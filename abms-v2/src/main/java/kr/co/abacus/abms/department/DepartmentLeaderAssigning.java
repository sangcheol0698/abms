package kr.co.abacus.abms.department;

/**
 * 부서장 지정 직전에 같은 트랜잭션 안에서 발행한다.
 * 직원 쪽이 받아서, 부서장으로 지정할 수 없는 직원이면 예외를 던진다.
 */
public record DepartmentLeaderAssigning(Long employeeId) {
}
