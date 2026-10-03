package kr.co.abacus.abms.notification;

import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.co.abacus.abms.common.web.Htmx;
import kr.co.abacus.abms.security.LoginUser;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public String dropdown(@AuthenticationPrincipal LoginUser user, Model model) {
        model.addAttribute("notifications", notificationService.recent(user.accountId(), 15));
        model.addAttribute("unread", notificationService.unreadCount(user.accountId()));
        return "notification/dropdown";
    }

    @PostMapping("/{id}/read")
    public String read(@AuthenticationPrincipal LoginUser user, @PathVariable Long id, HttpServletResponse response, Model model) {
        @Nullable String link = notificationService.read(user.accountId(), id);
        if (link != null && link.startsWith("/")) {
            Htmx.redirect(response, link);
            return "fragments/empty";
        }
        return dropdown(user, model);
    }

    @PostMapping("/read-all")
    public String readAll(@AuthenticationPrincipal LoginUser user, Model model) {
        notificationService.readAll(user.accountId());
        return dropdown(user, model);
    }

}
