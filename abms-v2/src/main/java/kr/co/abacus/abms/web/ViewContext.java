package kr.co.abacus.abms.web;

import org.jspecify.annotations.Nullable;
import org.springframework.security.web.csrf.CsrfToken;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.common.web.Toast;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 모든 페이지 템플릿에 전달되는 공통 컨텍스트 (로그인 사용자, CSRF, 현재 경로, 알림 등).
 */
public record ViewContext(
        @Nullable LoginUser user,
        @Nullable CsrfToken csrf,
        String path,
        @Nullable Toast toast,
        long unreadNotifications,
        boolean aiEnabled,
        @Nullable String mapKey,
        long unreadNotices,
        boolean noticePopup
) {

    /** 카카오 지도 사용 가능 여부 (키가 없으면 주소·바로가기 링크만 보여준다) */
    public boolean mapEnabled() {
        return mapKey != null;
    }

    public boolean loggedIn() {
        return user != null;
    }

    public LoginUser requireUser() {
        if (user == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return user;
    }

    public boolean can(PermissionCode code) {
        return user != null && user.has(code);
    }

    /** 사이드바 메뉴 활성화 판단 */
    public boolean active(String prefix) {
        if ("/".equals(prefix)) {
            return "/".equals(path);
        }
        return path.equals(prefix) || path.startsWith(prefix + "/");
    }

    public String csrfHeaderJson() {
        if (csrf == null) {
            return "{}";
        }
        return "{\"" + csrf.getHeaderName() + "\": \"" + csrf.getToken() + "\"}";
    }

    public String csrfParameter() {
        return csrf == null ? "_csrf" : csrf.getParameterName();
    }

    public String csrfToken() {
        return csrf == null ? "" : csrf.getToken();
    }

}
