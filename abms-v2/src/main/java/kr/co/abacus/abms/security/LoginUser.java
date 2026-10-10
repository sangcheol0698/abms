package kr.co.abacus.abms.security;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.access.PermissionScope;
import kr.co.abacus.abms.common.audit.AuditActor;

/**
 * 인증된 사용자. 로그인 시점의 권한(코드별 범위)을 보관한다.
 * 권한 그룹이 변경되면 다음 로그인부터 반영된다.
 */
public final class LoginUser implements UserDetails, CredentialsContainer, Serializable, AuditActor {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long accountId;
    private final Long employeeId;
    private final Long departmentId;
    private final String username;
    private final String name;
    private final @Nullable String photoUrl;
    private final boolean enabled;
    private final boolean locked;
    private final Map<PermissionCode, Set<PermissionScope>> grants;
    private @Nullable String password;

    public LoginUser(Long accountId, Long employeeId, Long departmentId, String username, String name,
                     @Nullable String photoUrl, @Nullable String password, boolean enabled, boolean locked,
                     Map<PermissionCode, Set<PermissionScope>> grants) {
        this.accountId = accountId;
        this.employeeId = employeeId;
        this.departmentId = departmentId;
        this.username = username;
        this.name = name;
        this.photoUrl = photoUrl;
        this.password = password;
        this.enabled = enabled;
        this.locked = locked;
        this.grants = Map.copyOf(grants);
    }

    public boolean has(PermissionCode code) {
        Set<PermissionScope> scopes = grants.get(code);
        return scopes != null && !scopes.isEmpty();
    }

    public boolean hasScope(PermissionCode code, PermissionScope scope) {
        return scopes(code).contains(scope);
    }

    public Set<PermissionScope> scopes(PermissionCode code) {
        Set<PermissionScope> scopes = grants.get(code);
        return scopes == null ? EnumSet.noneOf(PermissionScope.class) : EnumSet.copyOf(scopes);
    }

    public Map<PermissionCode, Set<PermissionScope>> grants() {
        return grants;
    }

    @Override
    public Long accountId() {
        return accountId;
    }

    public Long employeeId() {
        return employeeId;
    }

    public Long departmentId() {
        return departmentId;
    }

    @Override
    public String name() {
        return name;
    }

    /** 프로필 사진 주소 (없으면 null → 기본 아바타) */
    public @Nullable String photoUrl() {
        return photoUrl;
    }

    /** 본인 사진을 바꾼 뒤 세션의 로그인 정보를 갱신할 때 쓴다. */
    public LoginUser withPhotoUrl(@Nullable String photoUrl) {
        return new LoginUser(accountId, employeeId, departmentId, username, name, photoUrl, password, enabled, locked, grants);
    }

    public String initial() {
        return name.isEmpty() ? "?" : name.substring(0, 1);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new java.util.ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        grants.keySet().forEach(code -> authorities.add(new SimpleGrantedAuthority(code.code())));
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

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }

}
