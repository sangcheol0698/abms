package kr.co.abacus.abms.common.web;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.util.HtmlUtils;

import gg.jte.Content;

/**
 * 폼에 넣는 CSRF hidden 필드. HTMX 가 동작하지 않을 때(스크립트 로딩 전·실패)도 일반 POST 가 통과하도록
 * 모든 POST 폼에 {@code ${Csrf.field()}} 를 넣는다. 템플릿에 ViewContext 를 넘기지 않아도 현재 요청에서 토큰을 읽는다.
 */
public final class Csrf {

    private Csrf() {
    }

    public static Content field() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        Object token = attributes == null ? null : attributes.getAttribute(CsrfToken.class.getName(), RequestAttributes.SCOPE_REQUEST);
        if (!(token instanceof CsrfToken csrf)) {
            return output -> { };
        }
        String html = "<input type=\"hidden\" name=\"" + HtmlUtils.htmlEscape(csrf.getParameterName())
                + "\" value=\"" + HtmlUtils.htmlEscape(csrf.getToken()) + "\">";
        return output -> output.writeContent(html);
    }

}
