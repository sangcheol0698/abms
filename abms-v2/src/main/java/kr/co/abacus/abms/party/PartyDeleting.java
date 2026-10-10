package kr.co.abacus.abms.party;

/**
 * 협력사 삭제 직전에 같은 트랜잭션 안에서 발행한다.
 * 협력사를 참조하는 쪽(프로젝트)이 받아서, 삭제할 수 없으면 {@link kr.co.abacus.abms.common.domain.BusinessException} 을 던진다.
 */
public record PartyDeleting(Long partyId) {
}
