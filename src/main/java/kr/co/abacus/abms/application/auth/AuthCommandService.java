package kr.co.abacus.abms.application.auth;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.dto.LoginCommand;
import kr.co.abacus.abms.application.auth.dto.RegistrationConfirmCommand;
import kr.co.abacus.abms.application.auth.dto.RegistrationRequestCommand;
import kr.co.abacus.abms.application.auth.inbound.AuthManager;
import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.auth.outbound.CredentialAuthenticator;
import kr.co.abacus.abms.application.auth.outbound.RegistrationLinkSender;
import kr.co.abacus.abms.application.auth.outbound.RegistrationTokenRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.account.AccountAlreadyExistsException;
import kr.co.abacus.abms.domain.auth.InvalidRegistrationTokenException;
import kr.co.abacus.abms.domain.auth.RegistrationToken;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeNotFoundException;
import kr.co.abacus.abms.domain.shared.Email;

@RequiredArgsConstructor
@Transactional
@Service
public class AuthCommandService implements AuthManager {

    private final CredentialAuthenticator credentialAuthenticator;
    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final RegistrationTokenRepository registrationTokenRepository;
    private final RegistrationLinkSender registrationLinkSender;
    private final PasswordEncoder passwordEncoder;

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
        } catch (RuntimeException exception) {
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

        accountRepository.save(Account.create(
                registrationToken.getEmployeeId(),
                email.address(),
                encodedPassword
        ));
        registrationTokenRepository.delete(registrationToken);
    }

    @Override
    public void login(LoginCommand command) {
        credentialAuthenticator.authenticate(command.username(), command.password());
    }

    private void validateAlreadyRegistered(Email email) {
        if (accountRepository.findByUsername(email).isPresent()) {
            throw new AccountAlreadyExistsException("이미 가입된 계정입니다: " + email.address());
        }
    }

}
