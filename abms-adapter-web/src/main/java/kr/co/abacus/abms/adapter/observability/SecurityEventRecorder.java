package kr.co.abacus.abms.adapter.observability;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;

@Component
public class SecurityEventRecorder {

    private static final Logger log = LoggerFactory.getLogger("security-event");

    private final MeterRegistry meterRegistry;

    public SecurityEventRecorder(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordUnauthorized(HttpServletRequest request) {
        String path = PathNormalizer.normalize(request.getRequestURI());
        meterRegistry.counter("abms.security.authentication.failures.total", "path", path).increment();
        log.atWarn()
                .addKeyValue("event.category", "security")
                .addKeyValue("event.action", "unauthorized")
                .addKeyValue("path", path)
                .addKeyValue("method", request.getMethod())
                .log("security_event");
    }

    public void recordAccessDenied(HttpServletRequest request) {
        String path = PathNormalizer.normalize(request.getRequestURI());
        meterRegistry.counter("abms.security.access.denied.total", "path", path).increment();
        log.atWarn()
                .addKeyValue("event.category", "security")
                .addKeyValue("event.action", "access_denied")
                .addKeyValue("path", path)
                .addKeyValue("method", request.getMethod())
                .log("security_event");
    }

    public void recordSessionExpired(HttpServletRequest request) {
        String path = PathNormalizer.normalize(request.getRequestURI());
        meterRegistry.counter("abms.security.session.expired.total", "path", path).increment();
        log.atWarn()
                .addKeyValue("event.category", "security")
                .addKeyValue("event.action", "session_expired")
                .addKeyValue("path", path)
                .addKeyValue("method", request.getMethod())
                .log("security_event");
    }
}
