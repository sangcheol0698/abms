package kr.co.abacus.abms.application.observability;

import java.time.Duration;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

@Component
public class ApplicationMetricsRecorder {

    private final MeterRegistry meterRegistry;

    public ApplicationMetricsRecorder(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementAuthLogin(String outcome) {
        meterRegistry.counter("abms.auth.login.attempts", "outcome", outcome).increment();
    }

    public void recordMailSend(String outcome, long durationMs) {
        Tags tags = Tags.of("outcome", outcome);
        meterRegistry.counter("abms.mail.send.total", tags).increment();
        Timer.builder("abms.mail.send.duration")
                .tags(tags)
                .register(meterRegistry)
                .record(Duration.ofMillis(durationMs));
    }

    public void incrementSessionInvalidation() {
        meterRegistry.counter("abms.security.session.invalidations.total").increment();
    }

    public void recordChatRequest(String mode, String outcome, long durationMs) {
        Tags tags = Tags.of("mode", mode, "outcome", outcome);
        meterRegistry.counter("abms.chat.requests.total", tags).increment();
        Timer.builder("abms.chat.request.duration")
                .tags(tags)
                .register(meterRegistry)
                .record(Duration.ofMillis(durationMs));
    }

    public void incrementChatToolCall(String toolName) {
        meterRegistry.counter("abms.chat.tool.calls.total", "tool", toolName).increment();
    }

    public void incrementEmployeeAction(String action) {
        meterRegistry.counter("abms.employee.actions.total", "action", action).increment();
    }

    public void incrementProjectAction(String action) {
        meterRegistry.counter("abms.project.actions.total", "action", action).increment();
    }

    public void incrementProjectAssignmentAction(String action) {
        meterRegistry.counter("abms.project.assignment.actions.total", "action", action).increment();
    }
}
