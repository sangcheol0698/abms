package kr.co.abacus.abms.adapter.api.admin;

import java.util.List;

import jakarta.validation.Valid;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.adapter.api.admin.dto.PermissionCatalogResponse;
import kr.co.abacus.abms.adapter.security.CurrentActorResolver;
import kr.co.abacus.abms.adapter.api.admin.dto.PermissionGroupAccountAssignRequest;
import kr.co.abacus.abms.adapter.api.admin.dto.PermissionGroupAccountResponse;
import kr.co.abacus.abms.adapter.api.admin.dto.PermissionGroupCatalogResponse;
import kr.co.abacus.abms.adapter.api.admin.dto.PermissionGroupDetailResponse;
import kr.co.abacus.abms.adapter.api.admin.dto.PermissionGroupGrantResponse;
import kr.co.abacus.abms.adapter.api.admin.dto.PermissionGroupListResponse;
import kr.co.abacus.abms.adapter.api.admin.dto.PermissionGroupUpsertRequest;
import kr.co.abacus.abms.adapter.api.common.EnumResponse;
import kr.co.abacus.abms.application.permission.inbound.PermissionGroupAdminFinder;
import kr.co.abacus.abms.application.permission.inbound.PermissionGroupAdminManager;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType;

@RequiredArgsConstructor
@PreAuthorize("@permissionAuthorizationChecker.hasPermission(authentication, 'permission.group.manage')")
@RequestMapping("/api/admin")
@RestController
public class PermissionGroupAdminApi {

    private final PermissionGroupAdminFinder permissionGroupAdminFinder;
    private final PermissionGroupAdminManager permissionGroupAdminManager;
    private final CurrentActorResolver currentActorResolver;

    @GetMapping("/permission-groups")
    public List<PermissionGroupListResponse> searchGroups(
            @RequestParam(required = false) @Nullable String name,
            @RequestParam(required = false) @Nullable PermissionGroupType groupType
    ) {
        return permissionGroupAdminFinder.searchGroups(name, groupType).stream()
                .map(PermissionGroupListResponse::from)
                .toList();
    }

    @GetMapping("/permission-groups/catalog")
    public PermissionGroupCatalogResponse getCatalog() {
        var catalog = permissionGroupAdminFinder.getCatalog();
        return new PermissionGroupCatalogResponse(
                catalog.permissions().stream()
                        .map(PermissionCatalogResponse::from)
                        .toList(),
                catalog.scopes().stream()
                        .map(scope -> new EnumResponse(scope.name(), scope.getDescription(), scope.getOrder()))
                        .toList());
    }

    @GetMapping("/permission-groups/{id}")
    public PermissionGroupDetailResponse getGroup(@PathVariable Long id) {
        var detail = permissionGroupAdminFinder.findGroupDetail(id);
        return new PermissionGroupDetailResponse(
                detail.id(),
                detail.name(),
                detail.description(),
                detail.groupType().name(),
                detail.grants().stream().map(PermissionGroupGrantResponse::from).toList(),
                detail.accounts().stream().map(PermissionGroupAccountResponse::from).toList());
    }

    @PostMapping("/permission-groups")
    public Long createGroup(@RequestBody @Valid PermissionGroupUpsertRequest request) {
        return permissionGroupAdminManager.createGroup(request.toCommand());
    }

    @PutMapping("/permission-groups/{id}")
    public void updateGroup(@PathVariable Long id, @RequestBody @Valid PermissionGroupUpsertRequest request) {
        permissionGroupAdminManager.updateGroup(id, request.toCommand());
    }

    @DeleteMapping("/permission-groups/{id}")
    public void deleteGroup(@PathVariable Long id, Authentication authentication) {
        permissionGroupAdminManager.deleteGroup(id, currentActorResolver.resolve(authentication).accountId());
    }

    @GetMapping("/accounts")
    public List<PermissionGroupAccountResponse> searchAccounts(
            @RequestParam Long permissionGroupId,
            @RequestParam(required = false) @Nullable String keyword
    ) {
        return permissionGroupAdminFinder.searchAssignableAccounts(keyword, permissionGroupId).stream()
                .map(PermissionGroupAccountResponse::from)
                .toList();
    }

    @PostMapping("/permission-groups/{id}/accounts")
    public void assignAccount(@PathVariable Long id, @RequestBody @Valid PermissionGroupAccountAssignRequest request) {
        permissionGroupAdminManager.assignAccount(id, request.accountId());
    }

    @DeleteMapping("/permission-groups/{id}/accounts/{accountId}")
    public void unassignAccount(
            @PathVariable Long id,
            @PathVariable Long accountId,
            Authentication authentication
    ) {
        permissionGroupAdminManager.unassignAccount(
                id,
                accountId,
                currentActorResolver.resolve(authentication).accountId()
        );
    }
}
