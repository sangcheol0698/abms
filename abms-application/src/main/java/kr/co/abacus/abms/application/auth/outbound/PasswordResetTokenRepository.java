package kr.co.abacus.abms.application.auth.outbound;

import java.util.Optional;

import kr.co.abacus.abms.domain.auth.PasswordResetToken;
import kr.co.abacus.abms.domain.shared.Email;

public interface PasswordResetTokenRepository {

    PasswordResetToken save(PasswordResetToken passwordResetToken);

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findFirstByEmailOrderByCreatedAtDesc(Email email);

    void delete(PasswordResetToken passwordResetToken);

    void deleteAllByEmail(Email email);

}
