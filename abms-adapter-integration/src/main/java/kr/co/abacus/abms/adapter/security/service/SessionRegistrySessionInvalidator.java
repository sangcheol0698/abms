package kr.co.abacus.abms.adapter.security.service;

import java.util.Collection;

import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.security.CustomUserDetails;
import kr.co.abacus.abms.application.auth.outbound.SessionInvalidator;

@RequiredArgsConstructor
@Service
public class SessionRegistrySessionInvalidator implements SessionInvalidator {

    private final SessionRegistry sessionRegistry;
    private final MeterRegistry meterRegistry;

    @Override
    public void invalidateSessions(Collection<Long> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return;
        }

        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (!(principal instanceof CustomUserDetails userDetails)) {
                continue;
            }
            if (!accountIds.contains(userDetails.getAccountId())) {
                continue;
            }
            for (SessionInformation sessionInformation : sessionRegistry.getAllSessions(principal, false)) {
                sessionInformation.expireNow();
                meterRegistry.counter("abms.security.session.invalidations.total").increment();
            }
        }
    }
}
