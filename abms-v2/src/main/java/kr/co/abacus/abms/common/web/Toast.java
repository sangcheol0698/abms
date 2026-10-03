package kr.co.abacus.abms.common.web;

import java.io.Serializable;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 리다이렉트 후 화면에 띄울 알림 메시지 (flash attribute).
 */
public record Toast(String type, String message) implements Serializable {

    public static final String ATTRIBUTE = "toast";

    public static void success(RedirectAttributes attributes, String message) {
        attributes.addFlashAttribute(ATTRIBUTE, new Toast("success", message));
    }

    public static void error(RedirectAttributes attributes, String message) {
        attributes.addFlashAttribute(ATTRIBUTE, new Toast("error", message));
    }

}
