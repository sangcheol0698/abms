package kr.co.abacus.abms.application.notification.inbound;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.notification.outbound.NotificationRepository;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.notification.Notification;
import kr.co.abacus.abms.domain.notification.NotificationCreateRequest;
import kr.co.abacus.abms.domain.notification.NotificationNotFoundException;
import kr.co.abacus.abms.domain.notification.NotificationType;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("알림 관리 (NotificationManager)")
class NotificationManagerTest extends IntegrationTestBase {

    @Autowired
    private NotificationManager notificationManager;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AccountRepository accountRepository;

    private Long accountId;
    private Long otherAccountId;

    @BeforeEach
    void setUpAccounts() {
        accountId = accountRepository.save(Account.create(1000L, "notification-manager-1@abms.co", "{noop}pw")).getIdOrThrow();
        otherAccountId = accountRepository.save(Account.create(1001L, "notification-manager-2@abms.co", "{noop}pw")).getIdOrThrow();
        flushAndClear();
    }

    @Test
    @DisplayName("알림을 생성하면 계정에 속한 읽지 않은 알림으로 저장된다")
    void create() {
        Notification created = notificationManager.create(accountId, new NotificationCreateRequest(
                "신규 알림",
                "설명",
                null,
                "/notifications/1"
        ));
        flushAndClear();

        Notification found = entityManager.find(Notification.class, created.getId());
        assertThat(found.getAccountId()).isEqualTo(accountId);
        assertThat(found.getTitle()).isEqualTo("신규 알림");
        assertThat(found.getDescription()).isEqualTo("설명");
        assertThat(found.getType()).isEqualTo(NotificationType.INFO);
        assertThat(found.isRead()).isFalse();
        assertThat(found.getLink()).isEqualTo("/notifications/1");
    }

    @Test
    @DisplayName("본인 계정의 알림만 읽음 처리할 수 있다")
    void markAsRead() {
        Notification notification = notificationRepository.save(createNotification(accountId, "읽음 대상"));
        flushAndClear();

        notificationManager.markAsRead(accountId, notification.getId());
        flushAndClear();

        Notification found = entityManager.find(Notification.class, notification.getId());
        assertThat(found.isRead()).isTrue();
    }

    @Test
    @DisplayName("다른 계정의 알림을 읽음 처리하려고 하면 예외가 발생한다")
    void markAsRead_notFound() {
        Notification otherAccountNotification = notificationRepository.save(createNotification(otherAccountId, "다른 계정 알림"));
        flushAndClear();

        assertThatThrownBy(() -> notificationManager.markAsRead(accountId, otherAccountNotification.getId()))
                .isInstanceOf(NotificationNotFoundException.class)
                .hasMessage("알림을 찾을 수 없습니다: " + otherAccountNotification.getId());
    }

    @Test
    @DisplayName("전체 읽음 처리는 본인 계정의 읽지 않은 알림만 변경한다")
    void markAllAsRead() {
        Notification myFirst = notificationRepository.save(createNotification(accountId, "내 알림 1"));
        Notification mySecond = notificationRepository.save(createNotification(accountId, "내 알림 2"));
        Notification other = notificationRepository.save(createNotification(otherAccountId, "다른 계정 알림"));
        flushAndClear();

        notificationManager.markAllAsRead(accountId);
        flushAndClear();

        assertThat(entityManager.find(Notification.class, myFirst.getId()).isRead()).isTrue();
        assertThat(entityManager.find(Notification.class, mySecond.getId()).isRead()).isTrue();
        assertThat(entityManager.find(Notification.class, other.getId()).isRead()).isFalse();
    }

    @Test
    @DisplayName("전체 삭제는 본인 계정의 알림만 soft delete 한다")
    void clearAll() {
        Notification myNotification = notificationRepository.save(createNotification(accountId, "삭제 대상"));
        Notification otherNotification = notificationRepository.save(createNotification(otherAccountId, "유지 대상"));
        flushAndClear();

        notificationManager.clearAll(accountId);
        flushAndClear();

        Notification deleted = entityManager.find(Notification.class, myNotification.getId());
        Notification remained = entityManager.find(Notification.class, otherNotification.getId());

        assertThat(deleted.isDeleted()).isTrue();
        assertThat(remained.isDeleted()).isFalse();
        assertThat(notificationRepository.findAllByAccountIdAndDeletedFalseOrderByCreatedAtDesc(accountId)).isEmpty();
        assertThat(notificationRepository.findAllByAccountIdAndDeletedFalseOrderByCreatedAtDesc(otherAccountId))
                .extracting(Notification::getId)
                .containsExactly(otherNotification.getId());
    }

    private Notification createNotification(Long ownerAccountId, String title) {
        return Notification.create(ownerAccountId, new NotificationCreateRequest(
                title,
                "알림 설명",
                NotificationType.WARNING,
                "/notifications"
        ));
    }

}
