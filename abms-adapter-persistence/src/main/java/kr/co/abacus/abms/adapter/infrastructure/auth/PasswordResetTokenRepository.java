package kr.co.abacus.abms.adapter.infrastructure.auth;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.auth.PasswordResetToken;

public interface PasswordResetTokenRepository
        extends Repository<PasswordResetToken, Long>,
        kr.co.abacus.abms.application.auth.outbound.PasswordResetTokenRepository {
}
