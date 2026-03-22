package kr.co.abacus.abms.adapter.api.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MvcResult;

import kr.co.abacus.abms.adapter.security.CustomUserDetails;
import kr.co.abacus.abms.adapter.api.notification.dto.NotificationResponse;
import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.notification.outbound.NotificationRepository;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.notification.Notification;
import kr.co.abacus.abms.domain.notification.NotificationCreateRequest;
import kr.co.abacus.abms.domain.notification.NotificationType;
import kr.co.abacus.abms.domain.shared.Email;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("알림 API (NotificationApi)")
class NotificationApiTest extends ApiIntegrationTestBase {

    private static final String TEST_USERNAME = "notification-user@abms.co";

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private Long accountId;

    @BeforeEach
    void setUpAccount() {
        Account account = accountRepository.findByUsername(new Email(TEST_USERNAME))
                .orElseGet(() -> accountRepository.save(Account.create(9999L, TEST_USERNAME, "{noop}test-password")));
        flushAndClear();
        accountId = account.getIdOrThrow();
    }

    @Test
    @DisplayName("알림 목록을 조회한다")
    void getNotifications() throws Exception {
        notificationRepository.save(Notification.create(accountId, new NotificationCreateRequest(
                "테스트 알림",
                "알림 내용",
                NotificationType.INFO,
                "/"
        )));
        flushAndClear();

        MvcResult result = mockMvc.perform(get("/api/notifications")
                        .with(authentication(authenticateAccount())))
                .andExpect(status().isOk())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<NotificationResponse> response = objectMapper.readValue(
                responseBody,
                objectMapper.getTypeFactory().constructCollectionType(List.class, NotificationResponse.class));

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);
        assertThat(response.getFirst().title()).isEqualTo("테스트 알림");
        assertThat(response.getFirst().description()).isEqualTo("알림 내용");
        assertThat(response.getFirst().type()).isEqualTo("INFO");
        assertThat(response.getFirst().read()).isFalse();
    }

    @Test
    @DisplayName("알림 단건을 읽음 처리한다")
    void markAsRead() throws Exception {
        Notification notification = notificationRepository.save(Notification.create(accountId, new NotificationCreateRequest(
                "읽음 처리 대상",
                null,
                NotificationType.WARNING,
                null
        )));
        flushAndClear();

        mockMvc.perform(patch("/api/notifications/{id}/read", notification.getId())
                        .with(authentication(authenticateAccount())))
                .andExpect(status().isNoContent());

        flushAndClear();

        Notification found = notificationRepository.findByIdAndAccountIdAndDeletedFalse(notification.getId(), accountId)
                .orElseThrow();
        assertThat(found.isRead()).isTrue();
    }

    @Test
    @DisplayName("전체 알림을 읽음 처리한다")
    void markAllAsRead() throws Exception {
        notificationRepository.save(Notification.create(accountId, new NotificationCreateRequest(
                "알림 1",
                null,
                NotificationType.INFO,
                null
        )));
        notificationRepository.save(Notification.create(accountId, new NotificationCreateRequest(
                "알림 2",
                null,
                NotificationType.ERROR,
                null
        )));
        flushAndClear();

        mockMvc.perform(patch("/api/notifications/read-all")
                        .with(authentication(authenticateAccount())))
                .andExpect(status().isNoContent());

        flushAndClear();

        List<Notification> notifications = notificationRepository
                .findAllByAccountIdAndDeletedFalseOrderByCreatedAtDesc(accountId);
        assertThat(notifications).isNotEmpty();
        assertThat(notifications).allMatch(Notification::isRead);
    }

    @Test
    @DisplayName("전체 알림을 삭제한다")
    void clearAll() throws Exception {
        notificationRepository.save(Notification.create(accountId, new NotificationCreateRequest(
                "삭제 대상 1",
                null,
                NotificationType.INFO,
                null
        )));
        notificationRepository.save(Notification.create(accountId, new NotificationCreateRequest(
                "삭제 대상 2",
                null,
                NotificationType.SUCCESS,
                null
        )));
        flushAndClear();

        mockMvc.perform(delete("/api/notifications")
                        .with(authentication(authenticateAccount())))
                .andExpect(status().isNoContent());

        flushAndClear();

        List<Notification> notifications = notificationRepository
                .findAllByAccountIdAndDeletedFalseOrderByCreatedAtDesc(accountId);
        assertThat(notifications).isEmpty();
    }

    private UsernamePasswordAuthenticationToken authenticateAccount() {
        Account account = accountRepository.findByUsername(new Email(TEST_USERNAME)).orElseThrow();
        return UsernamePasswordAuthenticationToken.authenticated(new CustomUserDetails(account), null, List.of());
    }

}
