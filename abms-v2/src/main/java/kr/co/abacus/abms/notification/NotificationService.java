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

    /**
     * 같은 알림(제목·링크)을 since 이후 이미 보냈으면 다시 보내지 않는다. (정기 알림이 하루에 두 번 돌아도 한 번만)
     * @return 보냈으면 true
     */
    public boolean notifyEmployeeOnce(Long employeeId, NotificationType type, String title, @Nullable String description, String link,
                                      java.time.LocalDateTime since) {
        return accountRepository.findByEmployeeId(employeeId)
                .filter(account -> !notificationRepository.existsByAccountIdAndTitleAndLinkAndCreatedAtGreaterThanEqual(account.id(), title, link, since))
                .map(account -> {
                    notifyAccount(account.id(), type, title, description, link);
                    return true;
                })
                .orElse(false);
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
