package kr.co.abacus.abms.common.audit;

/**
 * 변경 이력에 남길 변경자. 인증 주체(principal)가 이 타입이면 {@link AuditEventListener} 가 계정 id·이름을 기록한다.
 * common 이 로그인 사용자 타입(security)을 직접 알지 않도록 둔 경계다.
 */
public interface AuditActor {

    Long accountId();

    String name();

}
