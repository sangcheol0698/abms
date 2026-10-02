package kr.co.abacus.abms.common.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HTMX 요청/응답 헤더 처리 도우미.
 */
public final class Htmx {

    public static final String HX_REQUEST = "HX-Request";
    public static final String HX_BOOSTED = "HX-Boosted";
    public static final String HX_TARGET = "HX-Target";

    private Htmx() {
    }

    /** 부분 갱신 요청 여부 (hx-boost 로 인한 전체 페이지 요청은 제외) */
    public static boolean isHtmx(HttpServletRequest request) {
        return "true".equals(request.getHeader(HX_REQUEST)) && !"true".equals(request.getHeader(HX_BOOSTED));
    }

    public static boolean targets(HttpServletRequest request, String targetId) {
        return isHtmx(request) && targetId.equals(request.getHeader(HX_TARGET));
    }

    public static void toast(HttpServletResponse response, ToastType type, String message) {
        trigger(response, "{\"toast\":{\"type\":\"" + type.value() + "\",\"message\":\"" + json(message) + "\"}}");
    }

    /** HX-Trigger 이벤트를 추가한다. 이미 설정된 이벤트가 있으면 합친다. */
    public static void trigger(HttpServletResponse response, String jsonObject) {
        String existing = response.getHeader("HX-Trigger");
        if (existing != null && existing.startsWith("{") && jsonObject.startsWith("{")) {
            jsonObject = existing.substring(0, existing.length() - 1) + "," + jsonObject.substring(1);
        }
        response.setHeader("HX-Trigger", jsonObject);
    }

    public static void event(HttpServletResponse response, String eventName) {
        trigger(response, "{\"" + eventName + "\":true}");
    }

    public static void redirect(HttpServletResponse response, String url) {
        response.setHeader("HX-Redirect", url);
    }

    /**
     * HTMX 요청(모달 폼 등) 처리 후 페이지를 이동시킨다. 토스트는 flash 로 전달되어 이동한 페이지에서 표시된다.
     *
     * @return 빈 응답 템플릿 이름
     */
    public static String redirect(HttpServletRequest request, HttpServletResponse response, String url, Toast toast) {
        org.springframework.web.servlet.FlashMap flash = org.springframework.web.servlet.support.RequestContextUtils.getOutputFlashMap(request);
        flash.put(Toast.ATTRIBUTE, toast);
        org.springframework.web.servlet.support.RequestContextUtils.saveOutputFlashMap(url, request, response);
        response.setHeader("HX-Redirect", url);
        return "fragments/empty";
    }

    /**
     * HTTP 헤더는 ISO-8859-1 만 허용하므로 비 ASCII 문자는 JSON 유니코드 이스케이프로 바꾼다.
     */
    static String json(String value) {
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20 || c > 0x7e) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    public enum ToastType {
        SUCCESS("success"), ERROR("error"), INFO("info");

        private final String value;

        ToastType(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

}
