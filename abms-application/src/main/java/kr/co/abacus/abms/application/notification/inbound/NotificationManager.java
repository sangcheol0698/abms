package kr.co.abacus.abms.application.notification.inbound;

import kr.co.abacus.abms.domain.notification.Notification;
import kr.co.abacus.abms.domain.notification.NotificationCreateRequest;

public interface NotificationManager {

    Notification create(Long accountId, NotificationCreateRequest request);

    void markAsRead(Long accountId, Long notificationId);

    void markAllAsRead(Long accountId);

    void clearAll(Long accountId);

}
