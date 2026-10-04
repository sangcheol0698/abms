package kr.co.abacus.abms.common.web;

import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

/**
 * 오류 페이지 모델. 서버 내부 메시지는 노출하지 않고, 우리가 안내용으로 지정한 문구(MESSAGE 속성)만 화면에 넘긴다.
 * (예: 보안 필터에서 막힌 화면의 "'계정 관리' 권한이 있어야 볼 수 있는 화면입니다.")
 */
@Component
public class ErrorPageAttributes extends DefaultErrorAttributes {

    public static final String MESSAGE = ErrorPageAttributes.class.getName() + ".message";

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> attributes = super.getErrorAttributes(webRequest, options);
        Object message = webRequest.getAttribute(MESSAGE, RequestAttributes.SCOPE_REQUEST);
        if (message != null) {
            attributes.put("message", message);
        }
        return attributes;
    }

}
