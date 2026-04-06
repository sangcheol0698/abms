package kr.co.abacus.abms.application.permission;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;

final class PermissionScopePolicy {

    private static final Set<PermissionScope> ALL_SCOPES = Set.copyOf(Arrays.asList(PermissionScope.values()));

    private static final Map<String, Set<PermissionScope>> ALLOWED_SCOPES_BY_PERMISSION = Map.of(
            "project.read", EnumSet.of(
                    PermissionScope.ALL,
                    PermissionScope.OWN_DEPARTMENT,
                    PermissionScope.OWN_DEPARTMENT_TREE,
                    PermissionScope.CURRENT_PARTICIPATION
            ),
            "project.write", EnumSet.of(
                    PermissionScope.ALL,
                    PermissionScope.OWN_DEPARTMENT,
                    PermissionScope.OWN_DEPARTMENT_TREE
            ),
            "project.excel.download", EnumSet.of(
                    PermissionScope.ALL,
                    PermissionScope.OWN_DEPARTMENT,
                    PermissionScope.OWN_DEPARTMENT_TREE,
                    PermissionScope.CURRENT_PARTICIPATION
            ),
            "project.excel.upload", EnumSet.of(
                    PermissionScope.ALL,
                    PermissionScope.OWN_DEPARTMENT,
                    PermissionScope.OWN_DEPARTMENT_TREE
            ),
            "report.read", EnumSet.of(PermissionScope.ALL),
            "party.read", EnumSet.of(PermissionScope.ALL),
            "party.write", EnumSet.of(PermissionScope.ALL)
    );

    private PermissionScopePolicy() {
    }

    static Set<PermissionScope> allowedScopesFor(String permissionCode) {
        Set<PermissionScope> scopes = ALLOWED_SCOPES_BY_PERMISSION.get(permissionCode);
        if (scopes == null) {
            return ALL_SCOPES;
        }
        return Set.copyOf(new LinkedHashSet<>(scopes));
    }

    static boolean isScopeAllowed(String permissionCode, PermissionScope scope) {
        return allowedScopesFor(permissionCode).contains(scope);
    }
}
