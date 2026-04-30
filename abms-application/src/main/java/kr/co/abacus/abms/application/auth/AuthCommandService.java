package kr.co.abacus.abms.application.auth;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.dto.ChangePasswordCommand;
import kr.co.abacus.abms.application.auth.dto.LoginCommand;
import kr.co.abacus.abms.application.auth.dto.PasswordResetConfirmCommand;
import kr.co.abacus.abms.application.auth.dto.PasswordResetRequestCommand;
import kr.co.abacus.abms.application.auth.dto.RegistrationConfirmCommand;
import kr.co.abacus.abms.application.auth.dto.RegistrationRequestCommand;
import kr.co.abacus.abms.application.auth.inbound.AuthManager;
import kr.co.abacus.abms.application.auth.outbound.AccountPermissionGroupRepository;
import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.auth.outbound.CredentialAuthenticator;
import kr.co.abacus.abms.application.auth.outbound.DefaultPermissionGroupRepository;
import kr.co.abacus.abms.application.auth.outbound.PasswordResetLinkSender;
import kr.co.abacus.abms.application.auth.outbound.PasswordResetTokenRepository;
import kr.co.abacus.abms.application.auth.outbound.RegistrationLinkSender;
import kr.co.abacus.abms.application.auth.outbound.RegistrationTokenRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.observability.ApplicationMetricsRecorder;
import kr.co.abacus.abms.application.observability.BusinessEventLogger;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.accountgroupassignment.AccountGroupAssignment;
import kr.co.abacus.abms.domain.account.AccountAlreadyExistsException;
import kr.co.abacus.abms.domain.account.AccountNotFoundException;
import kr.co.abacus.abms.domain.account.InvalidCurrentPasswordException;
import kr.co.abacus.abms.domain.account.SamePasswordException;
import kr.co.abacus.abms.domain.auth.InvalidPasswordResetTokenException;
import kr.co.abacus.abms.domain.auth.InvalidRegistrationTokenException;
import kr.co.abacus.abms.domain.auth.PasswordResetToken;
import kr.co.abacus.abms.domain.auth.RegistrationToken;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeNotFoundException;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroup;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType;
import kr.co.abacus.abms.domain.shared.Email;

@RequiredArgsConstructor
@Transactional
@Service
public class AuthCommandService implements AuthManager {

    private final CredentialAuthenticator credentialAuthenticator;
    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final RegistrationTokenRepository registrationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RegistrationLinkSender registrationLinkSender;
    private final PasswordResetLinkSender passwordResetLinkSender;
    private final PasswordEncoder passwordEncoder;
    private final DefaultPermissionGroupRepository defaultPermissionGroupRepository;
    private final AccountPermissionGroupRepository accountPermissionGroupRepository;
    private final BusinessEventLogger businessEventLogger;
    private final ApplicationMetricsRecorder applicationMetricsRecorder;

    @Override
    public void requestRegistration(RegistrationRequestCommand command) {
        Email email = new Email(command.email());
        validateAlreadyRegistered(email);

        Employee employee = employeeRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new EmployeeNotFoundException("가입 가능한 직원이 아닙니다: " + command.email()));

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);

        registrationTokenRepository.deleteAllByEmail(email);

        registrationTokenRepository.save(RegistrationToken.create(
                employee.getIdOrThrow(),
                command.email(),
                token,
                expiresAt
        ));

        try {
            registrationLinkSender.sendRegistrationLink(email, token, expiresAt);
            businessEventLogger.authEvent("registration_request", email.address(), "success", null);
        } catch (RuntimeException exception) {
            businessEventLogger.authEvent("registration_request", email.address(), "failure", exception.getClass().getSimpleName());
            throw new RegistrationMailSendException("가입 메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.", exception);
        }
    }

    @Override
    public void confirmRegistration(RegistrationConfirmCommand command) {
        RegistrationToken registrationToken = registrationTokenRepository.findByToken(command.token())
                .orElseThrow(() -> new InvalidRegistrationTokenException("유효하지 않은 가입 토큰입니다."));
        registrationToken.consume(LocalDateTime.now());

        Email email = registrationToken.getEmail();
        validateAlreadyRegistered(email);
        String encodedPassword = Objects.requireNonNull(passwordEncoder.encode(command.password()));

        Account account = accountRepository.save(Account.create(
                registrationToken.getEmployeeId(),
                email.address(),
                encodedPassword
        ));
        assignDefaultPermissionGroup(account);
        registrationTokenRepository.delete(registrationToken);
        businessEventLogger.authEvent("registration_confirm", email.address(), "success", null);
    }

    @Override
    public void login(LoginCommand command) {
        try {
            credentialAuthenticator.authenticate(command.username(), command.password());
            businessEventLogger.authEvent("login", command.username(), "success", null);
            applicationMetricsRecorder.incrementAuthLogin("success");
        } catch (RuntimeException exception) {
            businessEventLogger.authEvent("login", command.username(), "failure", exception.getClass().getSimpleName());
            applicationMetricsRecorder.incrementAuthLogin("failure");
            throw exception;
        }
    }

    @Override
    public void changePassword(ChangePasswordCommand command) {
        Email email = new Email(command.username());
        Account account = accountRepository.findByUsername(email)
                .orElseThrow(() -> new AccountNotFoundException("계정을 찾을 수 없습니다: " + email.address()));

        if (!passwordEncoder.matches(command.currentPassword(), account.getPassword())) {
            throw new InvalidCurrentPasswordException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (passwordEncoder.matches(command.newPassword(), account.getPassword())) {
            throw new SamePasswordException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        account.changePassword(Objects.requireNonNull(passwordEncoder.encode(command.newPassword())));
        businessEventLogger.authEvent("change_password", email.address(), "success", null);
    }

    @Override
    public void requestPasswordReset(PasswordResetRequestCommand command) {
        Email email = new Email(command.email());
        accountRepository.findByUsername(email)
                .filter(account -> Boolean.TRUE.equals(account.getIsValid()))
                .filter(account -> !account.isDeleted())
                .ifPresent(account -> issuePasswordResetToken(email, account));
    }

    @Override
    public void confirmPasswordReset(PasswordResetConfirmCommand command) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(command.token())
                .orElseThrow(() -> new InvalidPasswordResetTokenException("유효하지 않은 비밀번호 재설정 토큰입니다."));
        passwordResetToken.consume(LocalDateTime.now());

        Account account = accountRepository.findByIdAndDeletedFalse(passwordResetToken.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException("계정을 찾을 수 없습니다."));

        if (passwordEncoder.matches(command.password(), account.getPassword())) {
            throw new SamePasswordException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        account.changePassword(Objects.requireNonNull(passwordEncoder.encode(command.password())));
        passwordResetTokenRepository.delete(passwordResetToken);
        businessEventLogger.authEvent("password_reset_confirm", passwordResetToken.getEmail().address(), "success", null);
    }

    private void validateAlreadyRegistered(Email email) {
        if (accountRepository.findByUsername(email).isPresent()) {
            throw new AccountAlreadyExistsException("이미 가입된 계정입니다: " + email.address());
        }
    }

    private void issuePasswordResetToken(Email email, Account account) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(30);

        passwordResetTokenRepository.deleteAllByEmail(email);
        passwordResetTokenRepository.save(PasswordResetToken.create(
                account.getIdOrThrow(),
                email.address(),
                token,
                expiresAt
        ));

        try {
            passwordResetLinkSender.sendPasswordResetLink(email, token, expiresAt);
            businessEventLogger.authEvent("password_reset_request", email.address(), "success", null);
        } catch (RuntimeException exception) {
            businessEventLogger.authEvent("password_reset_request", email.address(), "failure", exception.getClass().getSimpleName());
            throw new PasswordResetMailSendException("비밀번호 재설정 메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.", exception);
        }
    }

    private void assignDefaultPermissionGroup(Account account) {
        PermissionGroup defaultPermissionGroup = defaultPermissionGroupRepository.findByGroupTypeAndNameAndDeletedFalse(
                PermissionGroupType.SYSTEM,
                DefaultPermissionGroupInitializer.DEFAULT_PERMISSION_GROUP_NAME
        ).orElseThrow(() -> new DefaultPermissionGroupNotConfiguredException(
                "기본 권한 그룹이 설정되지 않았습니다: " + DefaultPermissionGroupInitializer.DEFAULT_PERMISSION_GROUP_NAME
        ));

        if (accountPermissionGroupRepository.existsByAccountIdAndPermissionGroupIdAndDeletedFalse(
                account.getIdOrThrow(),
                defaultPermissionGroup.getIdOrThrow()
        )) {
            return;
        }

        accountPermissionGroupRepository.save(AccountGroupAssignment.create(
                account.getIdOrThrow(),
                defaultPermissionGroup.getIdOrThrow()
        ));
    }

}
