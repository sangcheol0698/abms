package kr.co.abacus.abms.application.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.ArgumentCaptor;

import kr.co.abacus.abms.application.auth.dto.ChangePasswordCommand;
import kr.co.abacus.abms.application.auth.dto.PasswordResetConfirmCommand;
import kr.co.abacus.abms.application.auth.dto.PasswordResetRequestCommand;
import kr.co.abacus.abms.application.auth.dto.RegistrationConfirmCommand;
import kr.co.abacus.abms.application.auth.dto.RegistrationRequestCommand;
import kr.co.abacus.abms.application.auth.inbound.AuthManager;
import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.auth.outbound.DefaultPermissionGroupRepository;
import kr.co.abacus.abms.application.auth.outbound.PasswordResetLinkSender;
import kr.co.abacus.abms.application.auth.outbound.PasswordResetTokenRepository;
import kr.co.abacus.abms.application.auth.outbound.RegistrationLinkSender;
import kr.co.abacus.abms.application.auth.outbound.RegistrationTokenRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.permission.outbound.AccountGroupAssignmentRepository;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.account.AccountAlreadyExistsException;
import kr.co.abacus.abms.domain.account.InvalidCurrentPasswordException;
import kr.co.abacus.abms.domain.account.SamePasswordException;
import kr.co.abacus.abms.domain.accountgroupassignment.AccountGroupAssignment;
import kr.co.abacus.abms.domain.auth.InvalidPasswordResetTokenException;
import kr.co.abacus.abms.domain.auth.PasswordResetToken;
import kr.co.abacus.abms.domain.auth.InvalidRegistrationTokenException;
import kr.co.abacus.abms.domain.auth.RegistrationToken;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeeNotFoundException;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroup;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType;
import kr.co.abacus.abms.domain.shared.Email;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("인증 관리 (AuthManager)")
class AuthManagerTest extends IntegrationTestBase {

    private static final String EMAIL = "auth-manager@abms.co";
    private static final String PASSWORD = "Password123!";

    @Autowired
    private AuthManager authManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RegistrationTokenRepository registrationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private RegistrationLinkSender registrationLinkSender;

    @Autowired
    private PasswordResetLinkSender passwordResetLinkSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DefaultPermissionGroupRepository defaultPermissionGroupRepository;

    @Autowired
    private AccountGroupAssignmentRepository accountGroupAssignmentRepository;

    @BeforeEach
    void resetRegistrationSender() {
        reset(registrationLinkSender);
        reset(passwordResetLinkSender);
    }

    @Test
    @DisplayName("회원가입 요청 시 기존 토큰을 정리하고 새 토큰을 발급한 뒤 메일을 발송한다")
    void requestRegistration() {
        Employee employee = employeeRepository.save(createEmployee(EMAIL, "가입요청직원"));
        RegistrationToken oldToken = registrationTokenRepository.save(RegistrationToken.create(
                employee.getIdOrThrow(),
                EMAIL,
                "old-token",
                LocalDateTime.now().plusMinutes(10)
        ));
        flushAndClear();

        authManager.requestRegistration(new RegistrationRequestCommand(EMAIL));
        flushAndClear();

        RegistrationToken latestToken = registrationTokenRepository.findFirstByEmailOrderByCreatedAtDesc(new Email(EMAIL))
                .orElseThrow();

        assertThat(latestToken.getToken()).isNotEqualTo("old-token");
        assertThat(latestToken.getEmployeeId()).isEqualTo(employee.getId());
        assertThat(latestToken.getExpiresAt()).isAfter(LocalDateTime.now().plusMinutes(29));
        assertThat(registrationTokenRepository.findByToken(oldToken.getToken())).isEmpty();
        ArgumentCaptor<LocalDateTime> expiresAtCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(registrationLinkSender).sendRegistrationLink(
                eq(new Email(EMAIL)),
                eq(latestToken.getToken()),
                expiresAtCaptor.capture()
        );
        assertThat(expiresAtCaptor.getValue().truncatedTo(ChronoUnit.SECONDS))
                .isEqualTo(latestToken.getExpiresAt().truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("이미 가입된 계정으로 회원가입 요청하면 예외가 발생한다")
    void requestRegistration_alreadyRegistered() {
        Employee employee = employeeRepository.save(createEmployee(EMAIL, "가입완료직원"));
        accountRepository.save(Account.create(employee.getIdOrThrow(), EMAIL, passwordEncoder.encode(PASSWORD)));
        flushAndClear();

        assertThatThrownBy(() -> authManager.requestRegistration(new RegistrationRequestCommand(EMAIL)))
                .isInstanceOf(AccountAlreadyExistsException.class)
                .hasMessage("이미 가입된 계정입니다: " + EMAIL);
        verifyNoInteractions(registrationLinkSender);
    }

    @Test
    @DisplayName("가입 가능한 직원이 아니면 회원가입 요청이 실패한다")
    void requestRegistration_employeeNotFound() {
        assertThatThrownBy(() -> authManager.requestRegistration(new RegistrationRequestCommand("missing@abms.co")))
                .isInstanceOf(EmployeeNotFoundException.class)
                .hasMessage("가입 가능한 직원이 아닙니다: missing@abms.co");
        verifyNoInteractions(registrationLinkSender);
    }

    @Test
    @DisplayName("회원가입 확정 시 계정을 생성하고 기본 권한 그룹을 연결한 뒤 토큰을 삭제한다")
    void confirmRegistration() {
        Employee employee = employeeRepository.save(createEmployee(EMAIL, "가입확정직원"));
        RegistrationToken registrationToken = registrationTokenRepository.save(RegistrationToken.create(
                employee.getIdOrThrow(),
                EMAIL,
                "confirm-token",
                LocalDateTime.now().plusMinutes(30)
        ));
        flushAndClear();

        authManager.confirmRegistration(new RegistrationConfirmCommand("confirm-token", PASSWORD));
        flushAndClear();

        Account account = accountRepository.findByUsername(new Email(EMAIL)).orElseThrow();
        assertThat(passwordEncoder.matches(PASSWORD, account.getPassword())).isTrue();
        assertThat(registrationTokenRepository.findByToken("confirm-token")).isEmpty();

        PermissionGroup defaultGroup = defaultPermissionGroupRepository.findByGroupTypeAndNameAndDeletedFalse(
                PermissionGroupType.SYSTEM,
                DefaultPermissionGroupInitializer.DEFAULT_PERMISSION_GROUP_NAME
        ).orElseThrow();

        List<AccountGroupAssignment> assignments = accountGroupAssignmentRepository.findAllByAccountIdAndDeletedFalse(
                account.getIdOrThrow()
        );
        assertThat(assignments)
                .extracting(AccountGroupAssignment::getPermissionGroupId)
                .containsExactly(defaultGroup.getIdOrThrow());
    }

    @Test
    @DisplayName("만료된 회원가입 토큰으로 확정하면 예외가 발생한다")
    void confirmRegistration_expiredToken() {
        Employee employee = employeeRepository.save(createEmployee(EMAIL, "만료토큰직원"));
        registrationTokenRepository.save(RegistrationToken.create(
                employee.getIdOrThrow(),
                EMAIL,
                "expired-token",
                LocalDateTime.now().minusMinutes(1)
        ));
        flushAndClear();

        assertThatThrownBy(() -> authManager.confirmRegistration(new RegistrationConfirmCommand("expired-token", PASSWORD)))
                .isInstanceOf(InvalidRegistrationTokenException.class)
                .hasMessage("만료된 가입 토큰입니다.");
        assertThat(accountRepository.findByUsername(new Email(EMAIL))).isEmpty();
    }

    @Test
    @DisplayName("이미 사용된 회원가입 토큰으로 다시 확정하면 예외가 발생한다")
    void confirmRegistration_usedToken() {
        Employee employee = employeeRepository.save(createEmployee(EMAIL, "재사용토큰직원"));
        RegistrationToken token = registrationTokenRepository.save(RegistrationToken.create(
                employee.getIdOrThrow(),
                EMAIL,
                "used-token",
                LocalDateTime.now().plusMinutes(10)
        ));
        token.consume(LocalDateTime.now());
        flushAndClear();

        assertThatThrownBy(() -> authManager.confirmRegistration(new RegistrationConfirmCommand("used-token", PASSWORD)))
                .isInstanceOf(InvalidRegistrationTokenException.class)
                .hasMessage("이미 사용된 가입 토큰입니다.");
        assertThat(accountRepository.findByUsername(new Email(EMAIL))).isEmpty();
    }

    @Test
    @DisplayName("현재 비밀번호가 맞으면 비밀번호를 변경한다")
    void changePassword() {
        Employee employee = employeeRepository.save(createEmployee(EMAIL, "비밀번호변경직원"));
        accountRepository.save(Account.create(employee.getIdOrThrow(), EMAIL, passwordEncoder.encode(PASSWORD)));
        flushAndClear();

        authManager.changePassword(new ChangePasswordCommand(EMAIL, PASSWORD, "ChangedPassword123!"));
        flushAndClear();

        Account changed = accountRepository.findByUsername(new Email(EMAIL)).orElseThrow();
        assertThat(passwordEncoder.matches("ChangedPassword123!", changed.getPassword())).isTrue();
        assertThat(passwordEncoder.matches(PASSWORD, changed.getPassword())).isFalse();
    }

    @Test
    @DisplayName("현재 비밀번호가 틀리면 비밀번호 변경이 실패한다")
    void changePassword_invalidCurrentPassword() {
        Employee employee = employeeRepository.save(createEmployee(EMAIL, "현재비밀번호오류직원"));
        accountRepository.save(Account.create(employee.getIdOrThrow(), EMAIL, passwordEncoder.encode(PASSWORD)));
        flushAndClear();

        assertThatThrownBy(() -> authManager.changePassword(
                new ChangePasswordCommand(EMAIL, "WrongPassword!", "ChangedPassword123!")))
                .isInstanceOf(InvalidCurrentPasswordException.class)
                .hasMessage("현재 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("새 비밀번호가 현재 비밀번호와 같으면 비밀번호 변경이 실패한다")
    void changePassword_samePassword() {
        Employee employee = employeeRepository.save(createEmployee(EMAIL, "동일비밀번호직원"));
        accountRepository.save(Account.create(employee.getIdOrThrow(), EMAIL, passwordEncoder.encode(PASSWORD)));
        flushAndClear();

        assertThatThrownBy(() -> authManager.changePassword(
                new ChangePasswordCommand(EMAIL, PASSWORD, PASSWORD)))
                .isInstanceOf(SamePasswordException.class)
                .hasMessage("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 시 토큰을 발급하고 메일을 발송한다")
    void requestPasswordReset() {
        Employee employee = employeeRepository.save(createEmployee(EMAIL, "비밀번호찾기직원"));
        Account account = accountRepository.save(Account.create(employee.getIdOrThrow(), EMAIL, passwordEncoder.encode(PASSWORD)));
        PasswordResetToken oldToken = passwordResetTokenRepository.save(PasswordResetToken.create(
                account.getIdOrThrow(),
                EMAIL,
                "old-reset-token",
                LocalDateTime.now().plusMinutes(10)
        ));
        flushAndClear();

        authManager.requestPasswordReset(new PasswordResetRequestCommand(EMAIL));
        flushAndClear();

        PasswordResetToken latestToken = passwordResetTokenRepository.findFirstByEmailOrderByCreatedAtDesc(new Email(EMAIL))
                .orElseThrow();

        assertThat(latestToken.getToken()).isNotEqualTo("old-reset-token");
        assertThat(latestToken.getAccountId()).isEqualTo(account.getId());
        assertThat(latestToken.getExpiresAt()).isAfter(LocalDateTime.now().plusMinutes(29));
        assertThat(passwordResetTokenRepository.findByToken(oldToken.getToken())).isEmpty();
        verify(passwordResetLinkSender).sendPasswordResetLink(
                eq(new Email(EMAIL)),
                eq(latestToken.getToken()),
                any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("가입되지 않은 이메일로 비밀번호 재설정을 요청해도 성공 처리하고 토큰을 만들지 않는다")
    void requestPasswordReset_unknownEmail() {
        authManager.requestPasswordReset(new PasswordResetRequestCommand("missing@iabacus.co.kr"));
        flushAndClear();

        assertThat(passwordResetTokenRepository.findFirstByEmailOrderByCreatedAtDesc(new Email("missing@iabacus.co.kr")))
                .isEmpty();
        verifyNoInteractions(passwordResetLinkSender);
    }

    @Test
    @DisplayName("유효한 토큰으로 비밀번호를 재설정한다")
    void confirmPasswordReset() {
        Employee employee = employeeRepository.save(createEmployee(EMAIL, "비밀번호재설정직원"));
        Account account = accountRepository.save(Account.create(employee.getIdOrThrow(), EMAIL, passwordEncoder.encode(PASSWORD)));
        passwordResetTokenRepository.save(PasswordResetToken.create(
                account.getIdOrThrow(),
                EMAIL,
                "reset-token",
                LocalDateTime.now().plusMinutes(30)
        ));
        flushAndClear();

        authManager.confirmPasswordReset(new PasswordResetConfirmCommand("reset-token", "ResetPassword123!"));
        flushAndClear();

        Account changed = accountRepository.findByUsername(new Email(EMAIL)).orElseThrow();
        assertThat(passwordEncoder.matches("ResetPassword123!", changed.getPassword())).isTrue();
        assertThat(passwordEncoder.matches(PASSWORD, changed.getPassword())).isFalse();
        assertThat(passwordResetTokenRepository.findByToken("reset-token")).isEmpty();
    }

    @Test
    @DisplayName("만료된 토큰으로 비밀번호 재설정하면 예외가 발생한다")
    void confirmPasswordReset_expiredToken() {
        Employee employee = employeeRepository.save(createEmployee(EMAIL, "만료재설정직원"));
        Account account = accountRepository.save(Account.create(employee.getIdOrThrow(), EMAIL, passwordEncoder.encode(PASSWORD)));
        passwordResetTokenRepository.save(PasswordResetToken.create(
                account.getIdOrThrow(),
                EMAIL,
                "expired-reset-token",
                LocalDateTime.now().minusMinutes(1)
        ));
        flushAndClear();

        assertThatThrownBy(() -> authManager.confirmPasswordReset(
                new PasswordResetConfirmCommand("expired-reset-token", "ResetPassword123!")))
                .isInstanceOf(InvalidPasswordResetTokenException.class)
                .hasMessage("만료된 비밀번호 재설정 토큰입니다.");
    }

    @Test
    @DisplayName("현재 비밀번호와 같은 비밀번호로 재설정하면 실패한다")
    void confirmPasswordReset_samePassword() {
        Employee employee = employeeRepository.save(createEmployee(EMAIL, "동일재설정직원"));
        Account account = accountRepository.save(Account.create(employee.getIdOrThrow(), EMAIL, passwordEncoder.encode(PASSWORD)));
        passwordResetTokenRepository.save(PasswordResetToken.create(
                account.getIdOrThrow(),
                EMAIL,
                "same-password-reset-token",
                LocalDateTime.now().plusMinutes(30)
        ));
        flushAndClear();

        assertThatThrownBy(() -> authManager.confirmPasswordReset(
                new PasswordResetConfirmCommand("same-password-reset-token", PASSWORD)))
                .isInstanceOf(SamePasswordException.class)
                .hasMessage("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
    }

    private Employee createEmployee(String email, String name) {
        return Employee.create(
                1L,
                name,
                email,
                LocalDate.of(2025, 1, 2),
                LocalDate.of(1995, 6, 10),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        );
    }

}
