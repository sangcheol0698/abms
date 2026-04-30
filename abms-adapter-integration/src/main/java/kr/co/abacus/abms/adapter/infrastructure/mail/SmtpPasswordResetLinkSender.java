package kr.co.abacus.abms.adapter.infrastructure.mail;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import io.micrometer.core.instrument.MeterRegistry;
import kr.co.abacus.abms.application.auth.outbound.PasswordResetLinkSender;
import kr.co.abacus.abms.domain.shared.Email;

@Profile("!test")
@Component
public class SmtpPasswordResetLinkSender implements PasswordResetLinkSender {

    private static final DateTimeFormatter EXPIRES_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String LOGO_CONTENT_ID = "abmsLogo";

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String subject;
    private final String confirmUrl;
    private final String logoPath;
    private final String logoUrl;
    private final MeterRegistry meterRegistry;

    public SmtpPasswordResetLinkSender(
            JavaMailSender mailSender,
            @Value("${app.auth.password-reset-mail.from}") String fromAddress,
            @Value("${app.auth.password-reset-mail.subject}") String subject,
            @Value("${app.auth.password-reset-mail.confirm-url}") String confirmUrl,
            @Value("${app.auth.password-reset-mail.logo-path:mail/main_logo.png}") String logoPath,
            @Value("${app.auth.password-reset-mail.logo-url:}") String logoUrl,
            MeterRegistry meterRegistry
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.subject = subject;
        this.confirmUrl = confirmUrl;
        this.logoPath = logoPath;
        this.logoUrl = logoUrl;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void sendPasswordResetLink(Email email, String token, LocalDateTime expiresAt) {
        String link = UriComponentsBuilder.fromUriString(confirmUrl)
                .queryParam("token", token)
                .build(true)
                .toUriString();
        long startedAt = System.nanoTime();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setTo(email.address());
            helper.setFrom(fromAddress);
            helper.setSubject(subject);
            helper.setText(buildPlainBody(link, expiresAt), buildHtmlBody(link, expiresAt));
            if (!StringUtils.hasText(logoUrl)) {
                helper.addInline(LOGO_CONTENT_ID, new ClassPathResource(logoPath), "image/png");
            }
            mailSender.send(message);
            recordMail("success", startedAt);
        } catch (MessagingException exception) {
            recordMail("failure", startedAt);
            throw new IllegalStateException("비밀번호 재설정 메일 생성 중 오류가 발생했습니다.", exception);
        } catch (RuntimeException exception) {
            recordMail("failure", startedAt);
            throw exception;
        }
    }

    private void recordMail(String outcome, long startedAt) {
        long durationMs = (System.nanoTime() - startedAt) / 1_000_000L;
        meterRegistry.counter("abms.mail.send.total", "type", "password_reset", "outcome", outcome).increment();
        meterRegistry.timer("abms.mail.send.duration", "type", "password_reset", "outcome", outcome)
                .record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    private String buildHtmlBody(String link, LocalDateTime expiresAt) {
        String logoSource = StringUtils.hasText(logoUrl) ? logoUrl : "cid:" + LOGO_CONTENT_ID;
        return """
                <div style="font-family: Arial, sans-serif; padding: 20px; max-width: 520px; margin: 0 auto; color: #333;">
                  <div style="margin-bottom: 20px;">
                    <img src="%s" alt="ABMS logo" style="height: 38px;" />
                  </div>
                  <h1 style="font-size: 24px; margin: 0 0 16px;">비밀번호 재설정 안내</h1>
                  <p style="font-size: 15px; color: #555; margin: 0 0 8px;">안녕하세요,</p>
                  <p style="font-size: 15px; color: #555; margin: 0 0 20px;">
                    아래 버튼을 눌러 ABMS 계정의 비밀번호를 재설정해 주세요.
                  </p>
                  <a href="%s" style="display: inline-block; background-color: #EB6129; color: #fff; padding: 12px 24px; font-size: 15px; text-decoration: none; border-radius: 6px; margin: 0 0 20px;">
                    비밀번호 재설정하기
                  </a>
                  <p style="font-size: 12px; color: #777; margin: 0 0 4px;">
                    버튼이 동작하지 않으면 아래 링크를 브라우저에 붙여넣어 주세요.
                  </p>
                  <p style="font-size: 12px; color: #EB6129; word-break: break-word; margin: 0 0 18px;">
                    %s
                  </p>
                  <p style="font-size: 12px; color: #777; margin: 0 0 4px;">
                    링크 만료 시각: %s
                  </p>
                  <p style="font-size: 11px; color: #999; margin: 16px 0 0;">
                    본인이 요청하지 않았다면 이 메일을 무시해 주세요.
                  </p>
                </div>
                """
                .formatted(logoSource, link, link, expiresAt.format(EXPIRES_AT_FORMATTER));
    }

    private String buildPlainBody(String link, LocalDateTime expiresAt) {
        return """
                ABMS 비밀번호 재설정 안내

                아래 링크를 브라우저에서 열어 비밀번호를 재설정해 주세요.
                %s

                링크 만료 시각: %s
                본인이 요청하지 않았다면 이 메일을 무시해 주세요.
                """.formatted(link, expiresAt.format(EXPIRES_AT_FORMATTER));
    }
}
