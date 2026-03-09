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
    public Notification create(Long accountId, NotificationCreateRequest request) {
        Notification notification = Notification.create(accountId, request);
        return notificationRepository.save(notification);
    }

    @Override
    public void markAsRead(Long accountId, Long notificationId) {
        Notification notification = findById(accountId, notificationId);
        notification.markAsRead();
    }

    @Override
    public void markAllAsRead(Long accountId) {
        notificationRepository.findAllByAccountIdAndDeletedFalseOrderByCreatedAtDesc(accountId)
                .forEach(Notification::markAsRead);
    }

    @Override
    public void clearAll(Long accountId) {
        notificationRepository.findAllByAccountIdAndDeletedFalseOrderByCreatedAtDesc(accountId)
                .forEach(notification -> notification.softDelete(accountId));
    }

    protected Notification findById(Long accountId, Long notificationId) {
        return notificationRepository.findByIdAndAccountIdAndDeletedFalse(notificationId, accountId)
                .orElseThrow(() -> new NotificationNotFoundException("알림을 찾을 수 없습니다: " + notificationId));
    }

}
