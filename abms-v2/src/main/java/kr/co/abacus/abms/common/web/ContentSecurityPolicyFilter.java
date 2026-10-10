package kr.co.abacus.abms.common.web;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Content-Security-Policy. 요청마다 nonce 를 만들어 nonce 가 붙은 스크립트만 실행한다.
 * 'strict-dynamic': nonce 로 허용된 스크립트(HTMX, 카카오 SDK 로더 등)가 동적으로 추가한 스크립트도 허용한다.
 * 스타일은 Tailwind 결과물과 style 속성(진행률 막대 등) 때문에 'unsafe-inline' 을 허용한다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ContentSecurityPolicyFilter extends OncePerRequestFilter {

    public static final String NONCE_ATTRIBUTE = ContentSecurityPolicyFilter.class.getName() + ".nonce";

    private static final SecureRandom RANDOM = new SecureRandom();

    /** 카카오맵(지도 타일·마커·SDK)과 Daum 우편번호 서비스 */
    private static final String KAKAO = "*.kakao.com *.daumcdn.net *.daum.net *.kakaocdn.net";

    /** BootUI 개발자 콘솔(로컬 개발 전용). 자체 번들 스크립트에 nonce 가 없어 이 정책을 걸면 화면이 뜨지 않는다. */
    private final String bootUiPath;

    public ContentSecurityPolicyFilter(@Value("${bootui.path:/bootui}") String bootUiPath) {
        this.bootUiPath = bootUiPath;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.equals(bootUiPath) || path.startsWith(bootUiPath + "/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        byte[] bytes = new byte[18];
        RANDOM.nextBytes(bytes);
        String nonce = Base64.getEncoder().encodeToString(bytes);
        request.setAttribute(NONCE_ATTRIBUTE, nonce);
        response.setHeader("Content-Security-Policy", policy(nonce));
        chain.doFilter(request, response);
    }

    static String policy(String nonce) {
        return String.join("; ",
                "default-src 'self'",
                "script-src 'nonce-" + nonce + "' 'strict-dynamic' 'self' https:",
                "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net",
                "font-src 'self' data: https://cdn.jsdelivr.net",
                "img-src 'self' data: blob: " + KAKAO,
                "connect-src 'self' " + KAKAO,
                "frame-src 'self' " + KAKAO,
                "object-src 'none'",
                "base-uri 'self'",
                "form-action 'self'",
                "frame-ancestors 'self'");
    }

}
