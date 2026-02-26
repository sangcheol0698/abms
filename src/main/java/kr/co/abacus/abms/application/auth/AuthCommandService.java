package kr.co.abacus.abms.application.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.dto.LoginCommand;
import kr.co.abacus.abms.application.auth.inbound.AuthManager;
import kr.co.abacus.abms.application.auth.outbound.CredentialAuthenticator;

@RequiredArgsConstructor
@Transactional
@Service
public class AuthCommandService implements AuthManager {

    private final CredentialAuthenticator credentialAuthenticator;

    @Override
    public void login(LoginCommand command) {
        credentialAuthenticator.authenticate(command.username(), command.password());
    }

}
