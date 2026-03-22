package kr.co.abacus.abms.adapter.security;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.auth.CurrentActorPrincipal;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;

public class CustomUserDetails implements UserDetails, CurrentActorPrincipal, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long accountId;
    private final String username;
    private final String password;
    private final @Nullable Long employeeId;
    private final @Nullable Long departmentId;
    private final Map<String, Set<PermissionScope>> permissionsByCode;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Account account) {
        this(account, null, null, Map.of());
    }

    public CustomUserDetails(
            Account account,
            @Nullable Long employeeId,
            @Nullable Long departmentId,
            Map<String, Set<PermissionScope>> permissionsByCode
    ) {
        this.accountId = account.getIdOrThrow();
        this.username = account.getUsername().address();
        this.password = account.getPassword();
        this.employeeId = employeeId;
        this.departmentId = departmentId;
        this.permissionsByCode = Collections.unmodifiableMap(new LinkedHashMap<>(permissionsByCode));
        this.authorities = this.permissionsByCode.keySet().stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    public Long getAccountId() {
        return accountId;
    }

    public @Nullable Long getEmployeeId() {
        return employeeId;
    }

    public @Nullable Long getDepartmentId() {
        return departmentId;
    }

    public Map<String, Set<PermissionScope>> getPermissionsByCode() {
        return permissionsByCode;
    }

    public CurrentActor toCurrentActor() {
        return new CurrentActor(accountId, username, employeeId, departmentId, permissionsByCode);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
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
