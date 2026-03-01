package kr.co.abacus.abms.adapter.infrastructure.auth;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.auth.RegistrationToken;

public interface RegistrationTokenRepository
        extends Repository<RegistrationToken, Long>,
        kr.co.abacus.abms.application.auth.outbound.RegistrationTokenRepository {
}
