package kr.co.abacus.abms.application.auth.outbound;

import java.time.LocalDateTime;

import kr.co.abacus.abms.domain.shared.Email;

public interface RegistrationLinkSender {

    void sendRegistrationLink(Email email, String token, LocalDateTime expiresAt);

}
