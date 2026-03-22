package kr.co.abacus.abms.application.auth;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.permission.dto.GrantedPermissionDetail;
import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;

public record CurrentActor(
        Long accountId,
        String username,
        @Nullable Long employeeId,
        @Nullable Long departmentId,
        Map<String, Set<PermissionScope>> permissionsByCode
) {

    public CurrentActor {
        permissionsByCode = normalizePermissions(permissionsByCode);
    }

    public boolean hasPermission(String permissionCode) {
        return permissionsByCode.containsKey(permissionCode);
    }

    public Set<PermissionScope> scopesOf(String permissionCode) {
        return permissionsByCode.getOrDefault(permissionCode, Set.of());
    }

    public List<GrantedPermissionDetail> grantedPermissions() {
        return permissionsByCode.entrySet().stream()
                .map(entry -> new GrantedPermissionDetail(entry.getKey(), entry.getValue()))
                .toList();
    }

    private static Map<String, Set<PermissionScope>> normalizePermissions(
            @Nullable Map<String, Set<PermissionScope>> permissionsByCode
    ) {
        if (permissionsByCode == null || permissionsByCode.isEmpty()) {
            return Map.of();
        }

        Map<String, Set<PermissionScope>> normalized = new LinkedHashMap<>();
        permissionsByCode.forEach((code, scopes) -> {
            if (code == null || code.isBlank()) {
                return;
            }
            normalized.put(
                    code,
                    java.util.Collections.unmodifiableSet(new LinkedHashSet<>(scopes != null ? scopes : Set.of()))
            );
        });
        return java.util.Collections.unmodifiableMap(new LinkedHashMap<>(normalized));
    }
}
