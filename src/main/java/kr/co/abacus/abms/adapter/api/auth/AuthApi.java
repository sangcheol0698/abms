package kr.co.abacus.abms.adapter.api.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.api.auth.dto.LoginRequest;
import kr.co.abacus.abms.adapter.api.auth.dto.RegistrationConfirmRequest;
import kr.co.abacus.abms.adapter.api.auth.dto.RegistrationRequest;
import kr.co.abacus.abms.application.auth.inbound.AuthManager;

@RequiredArgsConstructor
@RestController
public class AuthApi {

    private final AuthManager authManager;
    private final SecurityContextRepository securityContextRepository;

    @PostMapping("/api/auth/login")
    public void login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        authManager.login(request.toCommand());
        securityContextRepository.saveContext(
                SecurityContextHolder.getContext(),
                httpServletRequest,
                httpServletResponse
        );
    }

    @PostMapping("/api/auth/registration-requests")
    public void requestRegistration(@RequestBody @Valid RegistrationRequest request) {
        authManager.requestRegistration(request.toCommand());
    }

    @PostMapping("/api/auth/registration-confirmations")
    public void confirmRegistration(@RequestBody @Valid RegistrationConfirmRequest request) {
        authManager.confirmRegistration(request.toCommand());
    }

}
