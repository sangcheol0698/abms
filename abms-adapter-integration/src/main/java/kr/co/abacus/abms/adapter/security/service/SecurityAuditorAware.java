package kr.co.abacus.abms.adapter.security.service;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.security.CustomUserDetails;
import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.shared.Email;

@Component("auditorProvider")
@RequiredArgsConstructor
public class SecurityAuditorAware implements AuditorAware<Long> {

    private final AccountRepository accountRepository;

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return Optional.of(userDetails.getAccountId());
        }

        String username = authentication.getName();
        if (username == null || username.isBlank() || "anonymousUser".equals(username)) {
            return Optional.empty();
        }

        return accountRepository.findByUsername(new Email(username))
                .map(Account::getIdOrThrow);
    }

}
