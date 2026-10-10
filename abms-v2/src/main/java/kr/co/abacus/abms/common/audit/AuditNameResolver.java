package kr.co.abacus.abms.common.audit;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 변경 이력의 참조 id(부서·직원·협력사 …)를 현재 이름으로 바꾼다.
 * 각 기능의 리포지토리가 구현해 {@link AuditQueryService} 가 기능 패키지를 직접 알지 않게 한다.
 */
public interface AuditNameResolver {

    /** 참조 대상 종류. {@link AuditQueryService} 의 참조 속성 표에 쓰인 이름과 같다. (예: "Department") */
    String auditKind();

    /** id → 이름. 없는(삭제된) 대상은 빠진다. */
    Map<Long, String> auditNames(Collection<Long> ids);

    static <T> Map<Long, String> byId(Collection<T> items, Function<T, Long> id, Function<T, String> name) {
        return items.stream().collect(Collectors.toMap(id, name));
    }

}
