package kr.co.abacus.abms.adapter.api.auth;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.api.auth.dto.LoginRequest;
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

}
