package kr.co.abacus.abms.common.web;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import kr.co.abacus.abms.assistant.AssistantProperties;
import kr.co.abacus.abms.notification.NotificationService;
import kr.co.abacus.abms.security.LoginUser;

@ControllerAdvice
public class GlobalModelAdvice {

    private final NotificationService notificationService;
    private final AssistantProperties assistantProperties;

    public GlobalModelAdvice(NotificationService notificationService, AssistantProperties assistantProperties) {
        this.notificationService = notificationService;
        this.assistantProperties = assistantProperties;
    }

    @ModelAttribute("ctx")
    public ViewContext viewContext(HttpServletRequest request, Model model) {
        LoginUser user = currentUser();
        CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        Toast toast = (Toast) model.getAttribute(Toast.ATTRIBUTE);
        long unread = user == null || Htmx.isHtmx(request) ? 0 : notificationService.unreadCount(user.accountId());
        return new ViewContext(user, csrf, request.getRequestURI(), toast, unread, assistantProperties.isConfigured());
    }

    private static LoginUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser user) {
            return user;
        }
        return null;
    }

}
