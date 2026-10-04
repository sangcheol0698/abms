package kr.co.abacus.abms.common.audit;

import org.jspecify.annotations.Nullable;

/**
 * 변경 이력을 남기는 엔티티. 등록·수정·삭제 시 {@link AuditEventListener} 가 바뀐 속성을 기록한다.
 */
public interface Auditable {

    /** 엔티티 종류 표시명 (예: 프로젝트) */
    String auditLabel();

    /** 변경 당시 대상 이름 (예: 프로젝트명) */
    String auditName();

    /** 상위 엔티티. 상위 화면의 이력에 함께 보인다. (예: 매출 계획 → 프로젝트) */
    default @Nullable AuditRef auditParent() {
        return null;
    }

    record AuditRef(String type, Long id) {
    }

}
