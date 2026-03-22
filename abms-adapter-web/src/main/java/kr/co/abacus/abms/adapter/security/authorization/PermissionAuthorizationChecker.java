package kr.co.abacus.abms.adapter.security.authorization;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component("permissionAuthorizationChecker")
public class PermissionAuthorizationChecker {

    public boolean hasPermission(@Nullable Authentication authentication, String permissionCode) {
        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> permissionCode.equals(authority.getAuthority()));
    }

}
