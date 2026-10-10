package kr.co.abacus.abms.web;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import kr.co.abacus.abms.assistant.AssistantProperties;
import kr.co.abacus.abms.common.web.Htmx;
import kr.co.abacus.abms.common.web.MapProperties;
import kr.co.abacus.abms.common.web.Toast;
import kr.co.abacus.abms.notice.NoticeController;
import kr.co.abacus.abms.notice.NoticeService;
import kr.co.abacus.abms.notification.NotificationService;
import kr.co.abacus.abms.security.LoginUser;

@ControllerAdvice
public class GlobalModelAdvice {

    private final NotificationService notificationService;
    private final AssistantProperties assistantProperties;
    private final MapProperties mapProperties;
    private final NoticeService noticeService;

    public GlobalModelAdvice(NotificationService notificationService, AssistantProperties assistantProperties,
                             MapProperties mapProperties, NoticeService noticeService) {
        this.noticeService = noticeService;
        this.mapProperties = mapProperties;
        this.notificationService = notificationService;
        this.assistantProperties = assistantProperties;
    }

    @ModelAttribute("ctx")
    public ViewContext viewContext(HttpServletRequest request, Model model) {
        LoginUser user = currentUser();
        CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        Toast toast = (Toast) model.getAttribute(Toast.ATTRIBUTE);
        boolean fullPage = user != null && !Htmx.isHtmx(request);
        long unread = fullPage ? notificationService.unreadCount(user.accountId()) : 0;
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        NoticeService.Summary notices = fullPage
                ? noticeService.summary(user.accountId(), session == null ? java.util.Set.of() : NoticeController.closedPopups(session))
                : NoticeService.Summary.NONE;
        return new ViewContext(user, csrf, request.getRequestURI(), toast, unread, assistantProperties.isConfigured(),
                mapProperties.isConfigured() ? mapProperties.kakaoJsKey() : null, notices.unread(), notices.popup());
    }

    private static LoginUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser user) {
            return user;
        }
        return null;
    }

}
