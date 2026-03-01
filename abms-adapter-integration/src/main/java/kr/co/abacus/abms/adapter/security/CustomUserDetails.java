package kr.co.abacus.abms.adapter.security;

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import kr.co.abacus.abms.domain.account.Account;

public class CustomUserDetails implements UserDetails {

    private final String username;
    private final String password;

    public CustomUserDetails(Account account) {
        this.username = account.getUsername().address();
        this.password = account.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

}
