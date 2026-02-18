package kr.co.abacus.abms.adapter.api.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;

import kr.co.abacus.abms.adapter.api.notification.dto.NotificationResponse;
import kr.co.abacus.abms.application.notification.outbound.NotificationRepository;
import kr.co.abacus.abms.domain.notification.Notification;
import kr.co.abacus.abms.domain.notification.NotificationCreateRequest;
import kr.co.abacus.abms.domain.notification.NotificationType;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("알림 API (NotificationApi)")
class NotificationApiTest extends ApiIntegrationTestBase {

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("알림 목록을 조회한다")
    void getNotifications() {
        notificationRepository.save(Notification.create(new NotificationCreateRequest(
                "테스트 알림",
                "알림 내용",
                NotificationType.INFO,
                "/"
        )));
        flushAndClear();

        List<NotificationResponse> response = restTestClient.get()
                .uri("/api/notifications")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<NotificationResponse>>() {
                })
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.getFirst().title()).isEqualTo("테스트 알림");
        assertThat(response.getFirst().description()).isEqualTo("알림 내용");
        assertThat(response.getFirst().type()).isEqualTo("INFO");
        assertThat(response.getFirst().read()).isFalse();
    }

    @Test
    @DisplayName("알림 단건을 읽음 처리한다")
    void markAsRead() {
        Notification notification = notificationRepository.save(Notification.create(new NotificationCreateRequest(
                "읽음 처리 대상",
                null,
                NotificationType.WARNING,
                null
        )));
        flushAndClear();

        restTestClient.patch()
                .uri("/api/notifications/{id}/read", notification.getId())
                .exchange()
                .expectStatus().isNoContent();

        flushAndClear();

        Notification found = notificationRepository.findByIdAndDeletedFalse(notification.getId()).orElseThrow();
        assertThat(found.isRead()).isTrue();
    }

    @Test
    @DisplayName("전체 알림을 읽음 처리한다")
    void markAllAsRead() {
        notificationRepository.save(Notification.create(new NotificationCreateRequest(
                "알림 1",
                null,
                NotificationType.INFO,
                null
        )));
        notificationRepository.save(Notification.create(new NotificationCreateRequest(
                "알림 2",
                null,
                NotificationType.ERROR,
                null
        )));
        flushAndClear();

        restTestClient.patch()
                .uri("/api/notifications/read-all")
                .exchange()
                .expectStatus().isNoContent();

        flushAndClear();

        List<Notification> notifications = notificationRepository.findAllByDeletedFalseOrderByCreatedAtDesc();
        assertThat(notifications).isNotEmpty();
        assertThat(notifications).allMatch(Notification::isRead);
    }

    @Test
    @DisplayName("전체 알림을 삭제한다")
    void clearAll() {
        notificationRepository.save(Notification.create(new NotificationCreateRequest(
                "삭제 대상 1",
                null,
                NotificationType.INFO,
                null
        )));
        notificationRepository.save(Notification.create(new NotificationCreateRequest(
                "삭제 대상 2",
                null,
                NotificationType.SUCCESS,
                null
        )));
        flushAndClear();

        restTestClient.delete()
                .uri("/api/notifications")
                .exchange()
                .expectStatus().isNoContent();

        flushAndClear();

        List<Notification> notifications = notificationRepository.findAllByDeletedFalseOrderByCreatedAtDesc();
        assertThat(notifications).isEmpty();
    }

}
