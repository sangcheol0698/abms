package kr.co.abacus.abms.application.auth.outbound;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.auth.RegistrationToken;
import kr.co.abacus.abms.domain.shared.Email;

public interface RegistrationTokenRepository extends Repository<RegistrationToken, Long> {

    RegistrationToken save(RegistrationToken registrationToken);

    Optional<RegistrationToken> findByToken(String token);

    Optional<RegistrationToken> findFirstByEmailOrderByCreatedAtDesc(Email email);

    void delete(RegistrationToken registrationToken);

    void deleteAllByEmail(Email email);

}
