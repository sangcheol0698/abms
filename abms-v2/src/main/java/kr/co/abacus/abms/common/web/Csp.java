package kr.co.abacus.abms.common.web;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * 템플릿에서 스크립트 태그에 붙일 CSP nonce: {@code <script nonce="${Csp.nonce()}">}
 */
public final class Csp {

    private Csp() {
    }

    public static String nonce() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        Object nonce = attributes == null ? null : attributes.getAttribute(ContentSecurityPolicyFilter.NONCE_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        return nonce == null ? "" : nonce.toString();
    }

}
