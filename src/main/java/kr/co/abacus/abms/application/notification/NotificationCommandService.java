package kr.co.abacus.abms.application.notification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.notification.inbound.NotificationManager;
import kr.co.abacus.abms.application.notification.outbound.NotificationRepository;
import kr.co.abacus.abms.domain.notification.Notification;
import kr.co.abacus.abms.domain.notification.NotificationCreateRequest;
import kr.co.abacus.abms.domain.notification.NotificationNotFoundException;

@RequiredArgsConstructor
@Transactional
@Service
public class NotificationCommandService implements NotificationManager {

    private final NotificationRepository notificationRepository;

    @Override
    public Notification create(NotificationCreateRequest request) {
        Notification notification = Notification.create(request);
        return notificationRepository.save(notification);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = findById(notificationId);
        notification.markAsRead();
    }

    @Override
    public void markAllAsRead() {
        notificationRepository.findAllByDeletedFalseOrderByCreatedAtDesc()
                .forEach(Notification::markAsRead);
    }

    @Override
    public void clearAll() {
        notificationRepository.findAllByDeletedFalseOrderByCreatedAtDesc()
                .forEach(notification -> notification.softDelete("system"));
    }

    protected Notification findById(Long notificationId) {
        return notificationRepository.findByIdAndDeletedFalse(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("알림을 찾을 수 없습니다: " + notificationId));
    }

}
