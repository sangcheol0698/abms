package kr.co.abacus.abms.application.notification.inbound;

import kr.co.abacus.abms.domain.notification.Notification;
import kr.co.abacus.abms.domain.notification.NotificationCreateRequest;

public interface NotificationManager {

    Notification create(NotificationCreateRequest request);

    void markAsRead(Long notificationId);

    void markAllAsRead();

    void clearAll();

}
