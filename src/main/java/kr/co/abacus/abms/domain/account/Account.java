package kr.co.abacus.abms.domain.account;

import static java.util.Objects.*;

import java.util.UUID;

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
@Table(
    name = "account",
    uniqueConstraints = @UniqueConstraint(name = "UK_ACCOUNT_USERNAME", columnNames = "username")
)
public class Account extends AbstractEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Embedded
    @AttributeOverride(name = "address", column = @Column(name = "username", nullable = false, length = 100))
    private Email username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Nullable
    @Column(name = "token", length = 500)
    private String token;

    @Column(name = "is_valid", nullable = false)
    private Boolean isValid;

    @Column(name = "login_fail_count", nullable = false)
    private Integer loginFailCount;

    public static Account create(AccountCreateRequest request) {
        Account account = new Account();

        account.employeeId = requireNonNull(request.employeeId());
        account.username = new Email(requireNonNull(request.username()));
        account.password = requireNonNull(request.password()); // 비밀번호는 이미 암호화되어 들어온다고 가정
        account.isValid = true;
        account.loginFailCount = 0;

        return account;
    }

    public void login(String providedPassword) {
        // 실제로는 암호화된 비밀번호와 비교해야 하지만, 여기서는 단순화
        if (!this.password.equals(providedPassword)) {
            this.loginFailCount++;
            if (this.loginFailCount >= 5) {
                this.isValid = false;
                throw new IllegalStateException("로그인 실패 횟수 초과로 계정이 비활성화되었습니다");
            }
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        // 로그인 성공
        this.loginFailCount = 0;
    }

    public void logout() {
        this.token = null;
    }

    public void resetPassword(String newPassword) {
        this.password = requireNonNull(newPassword);
        this.loginFailCount = 0;
    }

    public void deactivate() {
        this.isValid = false;
    }

    public void activate() {
        this.isValid = true;
        this.loginFailCount = 0;
    }

}
