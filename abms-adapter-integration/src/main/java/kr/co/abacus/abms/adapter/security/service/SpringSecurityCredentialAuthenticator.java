package kr.co.abacus.abms.adapter.security.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import kr.co.abacus.abms.application.auth.outbound.CredentialAuthenticator;

@Slf4j
@RequiredArgsConstructor
@Service
public class SpringSecurityCredentialAuthenticator implements CredentialAuthenticator {

    private final AuthenticationManager authenticationManager;

    @Override
    public void authenticate(String username, String password) {
        Authentication request = UsernamePasswordAuthenticationToken.unauthenticated(username, password);
        Authentication authenticate = authenticationManager.authenticate(request);

        SecurityContextHolder.getContext().setAuthentication(authenticate);
        log.info("로그인 성공: {}", authenticate.getPrincipal());
    }

}
