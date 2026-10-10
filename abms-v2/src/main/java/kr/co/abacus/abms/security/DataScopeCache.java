package kr.co.abacus.abms.security;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

/**
 * 웹 요청 하나 동안 (사용자, 권한 코드)별 권한 범위를 보관한다.
 * 목록을 권한으로 걸러낼 때 항목마다 부서 트리·참여 프로젝트를 다시 조회하지 않기 위해서다.
 * 프록시 없는 request 스코프라, 요청 밖(스케줄러 등)에서는 {@link AccessService} 가 이 빈을 얻지 못하고 매번 계산한다.
 */
@Component
@Scope(WebApplicationContext.SCOPE_REQUEST)
class DataScopeCache {

    private final Map<String, DataScope> scopes = new HashMap<>();

    DataScope get(String key, Supplier<DataScope> compute) {
        return scopes.computeIfAbsent(key, k -> compute.get());
    }

}
