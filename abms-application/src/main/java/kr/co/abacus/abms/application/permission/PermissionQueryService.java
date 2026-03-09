package kr.co.abacus.abms.application.permission;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.permission.dto.GrantedPermissionDetail;
import kr.co.abacus.abms.application.permission.dto.PermissionDetail;
import kr.co.abacus.abms.application.permission.inbound.PermissionFinder;
import kr.co.abacus.abms.application.permission.outbound.AccountGroupAssignmentRepository;
import kr.co.abacus.abms.application.permission.outbound.GroupPermissionGrantRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionGroupRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionRepository;
import kr.co.abacus.abms.domain.accountgroupassignment.AccountGroupAssignment;
import kr.co.abacus.abms.domain.grouppermissiongrant.GroupPermissionGrant;
import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;
import kr.co.abacus.abms.domain.permission.Permission;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroup;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PermissionQueryService implements PermissionFinder {

    private static final Comparator<PermissionScope> SCOPE_ORDER =
            Comparator.comparingInt(PermissionScope::getOrder);

    private final AccountGroupAssignmentRepository accountGroupAssignmentRepository;
    private final PermissionGroupRepository permissionGroupRepository;
    private final GroupPermissionGrantRepository groupPermissionGrantRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public PermissionDetail findPermissions(Long accountId) {
        List<AccountGroupAssignment> assignments = accountGroupAssignmentRepository
                .findAllByAccountIdAndDeletedFalse(accountId);
        if (assignments.isEmpty()) {
            return new PermissionDetail(accountId, List.of());
        }

        List<Long> permissionGroupIds = assignments.stream()
                .map(AccountGroupAssignment::getPermissionGroupId)
                .distinct()
                .toList();

        Set<Long> activePermissionGroupIds = permissionGroupRepository.findAllByIdInAndDeletedFalse(permissionGroupIds)
                .stream()
                .map(PermissionGroup::getIdOrThrow)
                .collect(Collectors.toSet());
        if (activePermissionGroupIds.isEmpty()) {
            return new PermissionDetail(accountId, List.of());
        }

        List<GroupPermissionGrant> grants = groupPermissionGrantRepository.findAllByPermissionGroupIdInAndDeletedFalse(
                activePermissionGroupIds.stream().toList());
        if (grants.isEmpty()) {
            return new PermissionDetail(accountId, List.of());
        }

        List<Long> permissionIds = grants.stream()
                .map(GroupPermissionGrant::getPermissionId)
                .distinct()
                .toList();

        Map<Long, String> permissionCodeById = permissionRepository.findAllByIdInAndDeletedFalse(permissionIds)
                .stream()
                .collect(Collectors.toMap(Permission::getIdOrThrow, Permission::getCode));
        if (permissionCodeById.isEmpty()) {
            return new PermissionDetail(accountId, List.of());
        }

        Map<String, Set<PermissionScope>> scopesByCode = grants.stream()
                .filter(grant -> permissionCodeById.containsKey(grant.getPermissionId()))
                .collect(Collectors.groupingBy(
                        grant -> permissionCodeById.get(grant.getPermissionId()),
                        Collectors.mapping(
                                GroupPermissionGrant::getScope,
                                Collectors.toCollection(() -> new LinkedHashSet<>())
                        )
                ));

        List<GrantedPermissionDetail> permissions = scopesByCode.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new GrantedPermissionDetail(entry.getKey(), sortScopes(entry.getValue())))
                .toList();

        return new PermissionDetail(accountId, permissions);
    }

    private Set<PermissionScope> sortScopes(Set<PermissionScope> scopes) {
        return scopes.stream()
                .sorted(SCOPE_ORDER)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
