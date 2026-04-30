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
@Table(name = "tb_password_reset_token", uniqueConstraints = {
        @UniqueConstraint(name = "UK_PASSWORD_RESET_TOKEN", columnNames = "token")
})
public class PasswordResetToken extends AbstractEntity {

    @Column(name = "account_id", nullable = false)
    private Long accountId;

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

    public static PasswordResetToken create(Long accountId, String email, String token, LocalDateTime expiresAt) {
        PasswordResetToken passwordResetToken = new PasswordResetToken();

        passwordResetToken.accountId = Objects.requireNonNull(accountId);
        passwordResetToken.email = new Email(Objects.requireNonNull(email));
        passwordResetToken.token = Objects.requireNonNull(token);
        passwordResetToken.expiresAt = Objects.requireNonNull(expiresAt);
        passwordResetToken.used = false;

        return passwordResetToken;
    }

    public void consume(LocalDateTime now) {
        if (Boolean.TRUE.equals(used)) {
            throw new InvalidPasswordResetTokenException("이미 사용된 비밀번호 재설정 토큰입니다.");
        }
        if (expiresAt.isBefore(now)) {
            throw new InvalidPasswordResetTokenException("만료된 비밀번호 재설정 토큰입니다.");
        }

        this.used = true;
        this.usedAt = now;
    }

}
