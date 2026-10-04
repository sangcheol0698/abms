package kr.co.abacus.abms.common.web;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 지도 설정. 카카오 JavaScript 키가 없으면 지도 대신 주소와 카카오맵 바로가기 링크만 보여준다.
 * REST 키가 있으면 서버에서 주소를 좌표로 바꾼다. (브라우저에서 좌표를 찾지 못했을 때)
 * 키는 카카오 개발자 콘솔에서 발급받고, JavaScript 키는 사이트 도메인을 등록해야 동작한다.
 */
@ConfigurationProperties("abms.map")
public record MapProperties(@Nullable String kakaoJsKey, @Nullable String kakaoRestKey) {

    public boolean isConfigured() {
        return kakaoJsKey != null && !kakaoJsKey.isBlank();
    }

    public boolean geocodingEnabled() {
        return kakaoRestKey != null && !kakaoRestKey.isBlank();
    }

}
