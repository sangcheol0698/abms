package kr.co.abacus.abms.adapter.integration.observability;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component("mailConnectivity")
public class MailConnectivityHealthIndicator implements HealthIndicator {

    private final JavaMailSenderImpl mailSender;
    private final boolean enabled;

    public MailConnectivityHealthIndicator(
            JavaMailSenderImpl mailSender,
            @Value("${app.observability.health.mail.enabled:true}") boolean enabled
    ) {
        this.mailSender = mailSender;
        this.enabled = enabled;
    }

    @Override
    public Health health() {
        if (!enabled) {
            return Health.up().withDetail("status", "disabled").build();
        }
        try {
            mailSender.testConnection();
            return Health.up().build();
        } catch (Exception exception) {
            return Health.down(exception).build();
        }
    }
}
