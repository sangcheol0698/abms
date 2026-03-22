package kr.co.abacus.abms.adapter.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.auth.CurrentActorPrincipal;

@Component
public class CurrentActorResolver {

    public CurrentActor resolve(Authentication authentication) {
        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CurrentActorPrincipal actorPrincipal) {
            return actorPrincipal.toCurrentActor();
        }

        throw new AccessDeniedException("CurrentActor principal이 없습니다.");
    }
}
