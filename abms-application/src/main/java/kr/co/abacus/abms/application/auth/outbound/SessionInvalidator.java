package kr.co.abacus.abms.application.auth.outbound;

import java.util.Collection;

public interface SessionInvalidator {

    void invalidateSessions(Collection<Long> accountIds);
}
