package kr.co.abacus.abms.adapter.infrastructure.mail;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Properties;

import jakarta.mail.BodyPart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import kr.co.abacus.abms.domain.shared.Email;

@ExtendWith(MockitoExtension.class)
@DisplayName("SMTP 회원가입 링크 발송기")
class SmtpRegistrationLinkSenderTest {

    @Mock
    private JavaMailSender mailSender;

    @Captor
    private ArgumentCaptor<MimeMessage> messageCaptor;

    @Test
    @DisplayName("회원가입 링크 메일을 생성해 발송한다")
    void should_sendRegistrationLinkMail() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        SmtpRegistrationLinkSender sender = new SmtpRegistrationLinkSender(
                mailSender,
                "no-reply@iabacus.co.kr",
                "[ABMS] 회원가입 링크 안내",
                "http://localhost:5173/auths/registration-confirm",
                "mail/main_logo.png",
                ""
        );

        sender.sendRegistrationLink(
                new Email("new-user@iabacus.co.kr"),
                "token-value",
                LocalDateTime.of(2026, 1, 2, 13, 45)
        );

        verify(mailSender).send(messageCaptor.capture());
        MimeMessage message = messageCaptor.getValue();
        String htmlBody = extractHtmlBody(message.getContent());

        assertThat(message.getAllRecipients()).extracting(Object::toString).containsExactly("new-user@iabacus.co.kr");
        assertThat(message.getFrom()).extracting(Object::toString).containsExactly("no-reply@iabacus.co.kr");
        assertThat(message.getSubject()).isEqualTo("[ABMS] 회원가입 링크 안내");
        assertThat(hasInlineLogo(message.getContent())).isTrue();
        assertThat(htmlBody)
                .contains("token=token-value")
                .contains("링크 만료 시각: 2026-01-02 13:45");
    }

    @Test
    @DisplayName("confirm-url에 기존 쿼리가 있어도 token 파라미터를 추가한다")
    void should_appendTokenParam_whenConfirmUrlAlreadyHasQuery() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        SmtpRegistrationLinkSender sender = new SmtpRegistrationLinkSender(
                mailSender,
                "no-reply@iabacus.co.kr",
                "[ABMS] 회원가입 링크 안내",
                "http://localhost:5173/auths/registration-confirm?source=email",
                "mail/main_logo.png",
                ""
        );

        sender.sendRegistrationLink(
                new Email("new-user@iabacus.co.kr"),
                "another-token",
                LocalDateTime.of(2026, 1, 2, 13, 45)
        );

        verify(mailSender).send(messageCaptor.capture());
        String body = extractHtmlBody(messageCaptor.getValue().getContent());

        assertThat(body)
                .contains("source=email")
                .contains("token=another-token");
    }

    private String extractHtmlBody(Object content) throws Exception {
        if (content instanceof String string) {
            return string;
        }
        if (content instanceof MimeMultipart multipart) {
            for (int index = 0; index < multipart.getCount(); index++) {
                BodyPart part = multipart.getBodyPart(index);
                if (part.isMimeType("text/html")) {
                    Object partContent = part.getContent();
                    if (partContent instanceof String string) {
                        return string;
                    }
                }
                String nested = extractHtmlBody(part.getContent());
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private boolean hasInlineLogo(Object content) throws Exception {
        if (content instanceof MimeMultipart multipart) {
            for (int index = 0; index < multipart.getCount(); index++) {
                BodyPart part = multipart.getBodyPart(index);
                String[] contentIds = part.getHeader("Content-ID");
                if (contentIds != null && Arrays.stream(contentIds).anyMatch(header -> header.contains("abmsLogo"))) {
                    return true;
                }
                if (hasInlineLogo(part.getContent())) {
                    return true;
                }
            }
        }
        return false;
    }

}
