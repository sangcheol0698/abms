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
import kr.co.abacus.abms.domain.notification.NotificationType;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("알림 조회 (NotificationFinder)")
class NotificationFinderTest extends IntegrationTestBase {

    @Autowired
    private NotificationFinder notificationFinder;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private AccountRepository accountRepository;

    private Long accountId;
    private Long otherAccountId;

    @BeforeEach
    void setUpAccounts() {
        accountId = accountRepository.save(Account.create(2000L, "notification-finder-1@abms.co", "{noop}pw")).getIdOrThrow();
        otherAccountId = accountRepository.save(Account.create(2001L, "notification-finder-2@abms.co", "{noop}pw")).getIdOrThrow();
        flushAndClear();
    }

    @Test
    @DisplayName("알림 목록은 본인 계정의 삭제되지 않은 알림만 반환한다")
    void findAll() {
        Notification visible = notificationRepository.save(createNotification(accountId, "보이는 알림"));
        Notification deleted = notificationRepository.save(createNotification(accountId, "삭제된 알림"));
        Notification other = notificationRepository.save(createNotification(otherAccountId, "다른 계정 알림"));
        deleted.softDelete(accountId);
        flushAndClear();

        List<Notification> notifications = notificationFinder.findAll(accountId);

        assertThat(notifications)
                .extracting(Notification::getId)
                .containsExactly(visible.getId());
        assertThat(notifications)
                .extracting(Notification::getTitle)
                .containsExactly("보이는 알림");
        assertThat(notifications)
                .extracting(Notification::getId)
                .doesNotContain(deleted.getId(), other.getId());
    }

    private Notification createNotification(Long ownerAccountId, String title) {
        return Notification.create(ownerAccountId, new NotificationCreateRequest(
                title,
                "알림 설명",
                NotificationType.INFO,
                "/notifications"
        ));
    }

}
