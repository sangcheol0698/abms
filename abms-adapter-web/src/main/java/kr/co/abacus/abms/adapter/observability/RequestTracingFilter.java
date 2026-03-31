package kr.co.abacus.abms.adapter.observability;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.micrometer.core.instrument.MeterRegistry;
import kr.co.abacus.abms.application.auth.CurrentActorPrincipal;

@Component
public class RequestTracingFilter extends OncePerRequestFilter {

    private static final Logger accessLog = LoggerFactory.getLogger("http-access");
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    private final MeterRegistry meterRegistry;
    private final long slowRequestThresholdMs;

    public RequestTracingFilter(
            MeterRegistry meterRegistry,
            @Value("${app.observability.http.slow-request-threshold-ms:1000}") long slowRequestThresholdMs
    ) {
        this.meterRegistry = meterRegistry;
        this.slowRequestThresholdMs = slowRequestThresholdMs;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startedAt = System.nanoTime();
        String requestId = resolveRequestId(request);
        String traceId = UUID.randomUUID().toString();

        MDC.put("requestId", requestId);
        MDC.put("traceId", traceId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        Throwable failure = null;
        try {
            filterChain.doFilter(request, response);
        } catch (IOException | ServletException exception) {
            failure = exception;
            throw exception;
        } catch (RuntimeException exception) {
            failure = exception;
            throw exception;
        } catch (Error error) {
            failure = error;
            throw error;
        } finally {
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000L;
            String normalizedPath = PathNormalizer.normalize(request.getRequestURI());
            logAccess(request, response, normalizedPath, durationMs, failure);
            if (durationMs >= slowRequestThresholdMs) {
                meterRegistry.counter("abms.http.slow.requests.total", "path", normalizedPath).increment();
            }
            MDC.remove("requestId");
            MDC.remove("traceId");
        }
    }

    private void logAccess(
            HttpServletRequest request,
            HttpServletResponse response,
            String normalizedPath,
            long durationMs,
            Throwable failure
    ) {
        var builder = accessLog.atInfo()
                .addKeyValue("event.category", "http")
                .addKeyValue("event.action", "request_complete")
                .addKeyValue("http.method", request.getMethod())
                .addKeyValue("http.path", normalizedPath)
                .addKeyValue("http.status", response.getStatus())
                .addKeyValue("http.client_ip", resolveClientIp(request))
                .addKeyValue("durationMs", durationMs);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CurrentActorPrincipal actorPrincipal) {
            var actor = actorPrincipal.toCurrentActor();
            builder.addKeyValue("accountId", actor.accountId());
            if (actor.employeeId() != null) {
                builder.addKeyValue("employeeId", actor.employeeId());
            }
        }
        if (failure != null) {
            builder.addKeyValue("exception", failure.getClass().getSimpleName());
        }
        builder.log("http_request_completed");
    }

    private String resolveRequestId(HttpServletRequest request) {
        String candidate = request.getHeader(REQUEST_ID_HEADER);
        if (candidate == null || candidate.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return candidate;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
