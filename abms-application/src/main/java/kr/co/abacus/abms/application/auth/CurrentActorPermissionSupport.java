package kr.co.abacus.abms.application.auth;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;

@RequiredArgsConstructor
@Component
public class CurrentActorPermissionSupport {

    private final DepartmentTreeResolver departmentTreeResolver;

    public Set<PermissionScope> requirePermission(CurrentActor actor, String permissionCode, String message) {
        if (!actor.hasPermission(permissionCode)) {
            throw new AccessDeniedException(message);
        }
        return actor.scopesOf(permissionCode);
    }

    public void validateDepartmentAccess(CurrentActor actor, String permissionCode, Long departmentId, String message) {
        Set<PermissionScope> scopes = requirePermission(actor, permissionCode, message);
        if (scopes.contains(PermissionScope.ALL)) {
            return;
        }
        if (!resolveAllowedDepartmentIds(actor, scopes).contains(departmentId)) {
            throw new AccessDeniedException(message);
        }
    }

    public Set<Long> resolveAllowedDepartmentIds(CurrentActor actor, String permissionCode, String message) {
        return resolveAllowedDepartmentIds(actor, requirePermission(actor, permissionCode, message));
    }

    public Set<Long> resolveAllowedDepartmentIds(CurrentActor actor, Set<PermissionScope> scopes) {
        Set<Long> allowedDepartmentIds = new LinkedHashSet<>();
        if (actor.departmentId() != null && scopes.contains(PermissionScope.OWN_DEPARTMENT)) {
            allowedDepartmentIds.add(actor.departmentId());
        }
        if (actor.departmentId() != null && scopes.contains(PermissionScope.OWN_DEPARTMENT_TREE)) {
            allowedDepartmentIds.addAll(departmentTreeResolver.resolve(actor.departmentId()));
        }
        return allowedDepartmentIds;
    }
}
