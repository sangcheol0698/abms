package kr.co.abacus.abms.adapter.security.authorization;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.inbound.AuthFinder;
import kr.co.abacus.abms.application.permission.inbound.PermissionFinder;
import kr.co.abacus.abms.domain.account.AccountNotFoundException;

@RequiredArgsConstructor
@Component("permissionAuthorizationChecker")
public class PermissionAuthorizationChecker {

    private final AuthFinder authFinder;
    private final PermissionFinder permissionFinder;

    public boolean hasPermission(@Nullable Authentication authentication, String permissionCode) {
        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated()) {
            return false;
        }

        try {
            Long accountId = authFinder.getCurrentAccountId(authentication.getName());

            return permissionFinder.findPermissions(accountId).permissions().stream()
                    .anyMatch(permission -> permission.code().equals(permissionCode));
        } catch (AccountNotFoundException | IllegalArgumentException exception) {
            return false;
        }
    }

}
