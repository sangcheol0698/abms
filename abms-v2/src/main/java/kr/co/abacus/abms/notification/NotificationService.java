package kr.co.abacus.abms.notification;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.account.AccountRepository;
import kr.co.abacus.abms.common.domain.NotFoundException;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AccountRepository accountRepository;

    public NotificationService(NotificationRepository notificationRepository, AccountRepository accountRepository) {
        this.notificationRepository = notificationRepository;
        this.accountRepository = accountRepository;
    }

    public void notifyAccount(Long accountId, NotificationType type, String title, @Nullable String description, @Nullable String link) {
        notificationRepository.save(Notification.create(accountId, type, title, description, link));
    }

    /** 직원에게 계정이 있으면 알림을 보낸다. */
    public void notifyEmployee(Long employeeId, NotificationType type, String title, @Nullable String description, @Nullable String link) {
        accountRepository.findByEmployeeId(employeeId)
                .ifPresent(account -> notifyAccount(account.id(), type, title, description, link));
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long accountId) {
        return notificationRepository.countByAccountIdAndReadFalse(accountId);
    }

    @Transactional(readOnly = true)
    public List<Notification> recent(Long accountId, int limit) {
        return notificationRepository.findAllByAccountIdOrderByCreatedAtDesc(accountId, PageRequest.of(0, limit));
    }

    /** 알림을 읽음 처리하고 이동할 링크를 돌려준다. */
    public @Nullable String read(Long accountId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .filter(n -> n.getAccountId().equals(accountId))
                .orElseThrow(() -> NotFoundException.of("알림", notificationId));
        notification.markAsRead();
        return notification.getLink();
    }

    public void readAll(Long accountId) {
        notificationRepository.markAllAsRead(accountId);
    }

}
