package kr.co.abacus.abms.domain.account;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.AbstractEntity;
import kr.co.abacus.abms.domain.shared.Email;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_account", uniqueConstraints = {
        @UniqueConstraint(name = "UK_ACCOUNT_USERNAME", columnNames = "username"),
        @UniqueConstraint(name = "UK_ACCOUNT_EMPLOYEE_ID", columnNames = "employee_id")
})
public class Account extends AbstractEntity {

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Embedded
    @AttributeOverride(name = "address", column = @Column(name = "username", nullable = false, length = 100))
    private Email username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "password_changed_at", nullable = false)
    private LocalDateTime passwordChangedAt;

    @Column(name = "is_valid", nullable = false)
    private Boolean isValid;

    @Column(name = "login_fail_count", nullable = false)
    private Integer loginFailCount;

    public static Account create(Long employeeId, String username, String password) {
        Account account = new Account();

        account.employeeId = Objects.requireNonNull(employeeId);
        account.username = new Email(Objects.requireNonNull(username));
        account.password = Objects.requireNonNull(password);
        account.passwordChangedAt = LocalDateTime.now();
        account.isValid = true;
        account.loginFailCount = 0;

        return account;
    }

}
