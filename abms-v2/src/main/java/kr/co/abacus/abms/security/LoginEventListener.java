package kr.co.abacus.abms.security;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.account.AccountRepository;

/**
 * 로그인 성공/실패를 계정에 기록한다. 연속 실패가 누적되면 계정이 잠긴다.
 */
@Component
public class LoginEventListener {

    private final AccountRepository accountRepository;

    public LoginEventListener(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        if (event.getAuthentication().getPrincipal() instanceof LoginUser user) {
            accountRepository.findById(user.accountId()).ifPresent(account -> account.loginSucceeded());
        }
    }

    @Transactional
    @EventListener
    public void onBadCredentials(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        if (username == null) {
            return;
        }
        accountRepository.findByUsername(username.trim().toLowerCase()).ifPresent(account -> account.loginFailed());
    }

}
