package kr.co.abacus.abms.adapter.api.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.api.auth.dto.AuthMeResponse;
import kr.co.abacus.abms.adapter.security.CurrentActorResolver;
import kr.co.abacus.abms.adapter.api.auth.dto.ChangePasswordRequest;
import kr.co.abacus.abms.adapter.api.auth.dto.LoginRequest;
import kr.co.abacus.abms.adapter.api.auth.dto.PasswordResetConfirmRequest;
import kr.co.abacus.abms.adapter.api.auth.dto.PasswordResetRequest;
import kr.co.abacus.abms.adapter.api.auth.dto.RegistrationConfirmRequest;
import kr.co.abacus.abms.adapter.api.auth.dto.RegistrationRequest;
import kr.co.abacus.abms.application.auth.inbound.AuthFinder;
import kr.co.abacus.abms.application.auth.inbound.AuthManager;

@RequiredArgsConstructor
@RestController
public class AuthApi {

    private final AuthManager authManager;
    private final AuthFinder authFinder;
    private final CurrentActorResolver currentActorResolver;
    private final SessionRegistry sessionRegistry;
    private final SecurityContextRepository securityContextRepository;

    @PostMapping("/api/auth/login")
    public void login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        authManager.login(request.toCommand());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        securityContextRepository.saveContext(
                SecurityContextHolder.getContext(),
                httpServletRequest,
                httpServletResponse
        );
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal != null) {
                sessionRegistry.registerNewSession(httpServletRequest.getSession().getId(), principal);
            }
        }
    }

    @PostMapping("/api/auth/registration-requests")
    public void requestRegistration(@RequestBody @Valid RegistrationRequest request) {
        authManager.requestRegistration(request.toCommand());
    }

    @PostMapping("/api/auth/registration-confirmations")
    public void confirmRegistration(@RequestBody @Valid RegistrationConfirmRequest request) {
        authManager.confirmRegistration(request.toCommand());
    }

    @PostMapping("/api/auth/password-reset-requests")
    public void requestPasswordReset(@RequestBody @Valid PasswordResetRequest request) {
        authManager.requestPasswordReset(request.toCommand());
    }

    @PostMapping("/api/auth/password-reset-confirmations")
    public void confirmPasswordReset(@RequestBody @Valid PasswordResetConfirmRequest request) {
        authManager.confirmPasswordReset(request.toCommand());
    }

    @GetMapping("/api/auth/me")
    public AuthMeResponse me(Authentication authentication) {
        return AuthMeResponse.from(authFinder.getCurrentUser(currentActorResolver.resolve(authentication)));
    }

    @PatchMapping("/api/auth/password")
    public void changePassword(
            Authentication authentication,
            @RequestBody @Valid ChangePasswordRequest request
    ) {
        authManager.changePassword(request.toCommand(authentication.getName()));
    }

}
