package kr.co.abacus.abms.domain.auth;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.jspecify.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.AbstractEntity;
import kr.co.abacus.abms.domain.shared.Email;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_registration_token", uniqueConstraints = {
        @UniqueConstraint(name = "UK_REGISTRATION_TOKEN", columnNames = "token")
})
public class RegistrationToken extends AbstractEntity {

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Embedded
    @AttributeOverride(name = "address", column = @Column(name = "email", nullable = false, length = 100))
    private Email email;

    @Column(name = "token", nullable = false, length = 100)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    private Boolean used;

    @Nullable
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    public static RegistrationToken create(Long employeeId, String email, String token, LocalDateTime expiresAt) {
        RegistrationToken registrationToken = new RegistrationToken();

        registrationToken.employeeId = Objects.requireNonNull(employeeId);
        registrationToken.email = new Email(Objects.requireNonNull(email));
        registrationToken.token = Objects.requireNonNull(token);
        registrationToken.expiresAt = Objects.requireNonNull(expiresAt);
        registrationToken.used = false;

        return registrationToken;
    }

    public void consume(LocalDateTime now) {
        if (Boolean.TRUE.equals(used)) {
            throw new InvalidRegistrationTokenException("이미 사용된 가입 토큰입니다.");
        }
        if (expiresAt.isBefore(now)) {
            throw new InvalidRegistrationTokenException("만료된 가입 토큰입니다.");
        }

        this.used = true;
        this.usedAt = now;
    }

}
