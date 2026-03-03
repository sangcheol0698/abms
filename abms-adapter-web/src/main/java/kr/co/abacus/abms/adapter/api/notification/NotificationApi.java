package kr.co.abacus.abms.adapter.api.notification;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.api.notification.dto.NotificationCreateApiRequest;
import kr.co.abacus.abms.adapter.api.notification.dto.NotificationResponse;
import kr.co.abacus.abms.application.auth.inbound.AuthFinder;
import kr.co.abacus.abms.application.notification.inbound.NotificationFinder;
import kr.co.abacus.abms.application.notification.inbound.NotificationManager;
import kr.co.abacus.abms.domain.notification.Notification;

@RequiredArgsConstructor
@RestController
public class NotificationApi {

    private final NotificationFinder notificationFinder;
    private final NotificationManager notificationManager;
    private final AuthFinder authFinder;

    @GetMapping("/api/notifications")
    public List<NotificationResponse> getNotifications(Authentication authentication) {
        Long accountId = resolveAccountId(authentication);
        return notificationFinder.findAll(accountId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @PostMapping("/api/notifications")
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse create(@RequestBody NotificationCreateApiRequest request, Authentication authentication) {
        Long accountId = resolveAccountId(authentication);
        Notification notification = notificationManager.create(accountId, request.toDomainRequest());
        return NotificationResponse.from(notification);
    }

    @PatchMapping("/api/notifications/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(@PathVariable Long id, Authentication authentication) {
        Long accountId = resolveAccountId(authentication);
        notificationManager.markAsRead(accountId, id);
    }

    @PatchMapping("/api/notifications/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllAsRead(Authentication authentication) {
        Long accountId = resolveAccountId(authentication);
        notificationManager.markAllAsRead(accountId);
    }

    @DeleteMapping("/api/notifications")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearAll(Authentication authentication) {
        Long accountId = resolveAccountId(authentication);
        notificationManager.clearAll(accountId);
    }

    private Long resolveAccountId(Authentication authentication) {
        return authFinder.getCurrentAccountId(authentication.getName());
    }

}
