package kr.co.abacus.abms.account;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;
import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.domain.BaseEntity;

/**
 * 로그인 계정. 직원 1명당 최대 1개.
 */
@Entity
@Table(name = "tb_account")
@SQLRestriction("deleted = false")
public class Account extends BaseEntity {

    public static final int MAX_LOGIN_FAILURES = 5;

    @Column(nullable = false, unique = true)
    private Long employeeId;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private LocalDateTime passwordChangedAt;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private int loginFailCount;

    private @Nullable LocalDateTime lastLoginAt;

    protected Account() {
    }

    public static Account create(Long employeeId, String username, String encodedPassword) {
        Account account = new Account();
        account.employeeId = Objects.requireNonNull(employeeId);
        account.username = username.trim().toLowerCase();
        account.password = Objects.requireNonNull(encodedPassword);
        account.passwordChangedAt = LocalDateTime.now();
        account.enabled = true;
        account.loginFailCount = 0;
        return account;
    }

    public void changePassword(String encodedPassword) {
        this.password = Objects.requireNonNull(encodedPassword);
        this.passwordChangedAt = LocalDateTime.now();
    }

    public void loginSucceeded() {
        this.loginFailCount = 0;
        this.lastLoginAt = LocalDateTime.now();
    }

    public void loginFailed() {
        this.loginFailCount++;
    }

    public boolean isLocked() {
        return loginFailCount >= MAX_LOGIN_FAILURES;
    }

    public void unlock() {
        this.loginFailCount = 0;
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public LocalDateTime getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getLoginFailCount() {
        return loginFailCount;
    }

    public @Nullable LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

}
