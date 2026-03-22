package kr.co.abacus.abms.application.permission;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.outbound.SessionInvalidator;
import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.permission.dto.PermissionGroupGrantCommand;
import kr.co.abacus.abms.application.permission.dto.PermissionGroupUpsertCommand;
import kr.co.abacus.abms.application.permission.inbound.PermissionGroupAdminManager;
import kr.co.abacus.abms.application.permission.outbound.AccountGroupAssignmentRepository;
import kr.co.abacus.abms.application.permission.outbound.GroupPermissionGrantRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionGroupRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionRepository;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.account.AccountNotFoundException;
import kr.co.abacus.abms.domain.accountgroupassignment.AccountGroupAssignment;
import kr.co.abacus.abms.domain.grouppermissiongrant.GroupPermissionGrant;
import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;
import kr.co.abacus.abms.domain.permission.Permission;
import kr.co.abacus.abms.domain.permission.PermissionNotFoundException;
import kr.co.abacus.abms.domain.permissiongroup.DuplicatePermissionGroupNameException;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroup;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupNotFoundException;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType;
import kr.co.abacus.abms.domain.permissiongroup.SystemPermissionGroupModificationDeniedException;

@RequiredArgsConstructor
@Transactional
@Service
public class PermissionGroupAdminModifyService implements PermissionGroupAdminManager {

    private final PermissionGroupRepository permissionGroupRepository;
    private final PermissionRepository permissionRepository;
    private final GroupPermissionGrantRepository groupPermissionGrantRepository;
    private final AccountGroupAssignmentRepository accountGroupAssignmentRepository;
    private final AccountRepository accountRepository;
    private final SessionInvalidator sessionInvalidator;

    @Override
    public Long createGroup(PermissionGroupUpsertCommand command) {
        validateUniqueCustomGroupName(command.name(), null);

        PermissionGroup group = permissionGroupRepository.save(PermissionGroup.create(
                command.name().trim(),
                command.description().trim(),
                PermissionGroupType.CUSTOM));
        syncGrants(group.getIdOrThrow(), command.grants());
        return group.getIdOrThrow();
    }

    @Override
    public void updateGroup(Long id, PermissionGroupUpsertCommand command) {
        PermissionGroup group = findActiveGroup(id);
        ensureCustomGroup(group);
        validateUniqueCustomGroupName(command.name(), id);
        List<Long> affectedAccountIds = findActiveAccountIds(group.getIdOrThrow());

        group.updateInfo(command.name().trim(), command.description().trim());
        permissionGroupRepository.save(group);
        syncGrants(group.getIdOrThrow(), command.grants());
        sessionInvalidator.invalidateSessions(affectedAccountIds);
    }

    @Override
    public void deleteGroup(Long id, Long actorAccountId) {
        PermissionGroup group = findActiveGroup(id);
        ensureCustomGroup(group);
        List<Long> affectedAccountIds = findActiveAccountIds(group.getIdOrThrow());

        accountGroupAssignmentRepository.findAllByPermissionGroupIdAndDeletedFalse(group.getIdOrThrow())
                .forEach(assignment -> assignment.softDelete(actorAccountId));
        groupPermissionGrantRepository.findAllByPermissionGroupIdAndDeletedFalse(group.getIdOrThrow())
                .forEach(grant -> grant.softDelete(actorAccountId));
        group.softDelete(actorAccountId);

        permissionGroupRepository.save(group);
        sessionInvalidator.invalidateSessions(affectedAccountIds);
    }

    @Override
    public void assignAccount(Long permissionGroupId, Long accountId) {
        PermissionGroup group = findActiveGroup(permissionGroupId);
        Account account = accountRepository.findByIdAndDeletedFalse(accountId)
                .orElseThrow(() -> new AccountNotFoundException("계정을 찾을 수 없습니다: " + accountId));

        accountGroupAssignmentRepository.findByAccountIdAndPermissionGroupId(account.getIdOrThrow(), group.getIdOrThrow())
                .ifPresentOrElse(existing -> {
                    if (!existing.isDeleted()) {
                        throw new IllegalArgumentException("이미 권한 그룹에 할당된 계정입니다.");
                    }
                    existing.restore();
                    accountGroupAssignmentRepository.save(existing);
                }, () -> accountGroupAssignmentRepository.save(AccountGroupAssignment.create(
                        account.getIdOrThrow(),
                        group.getIdOrThrow())));
        sessionInvalidator.invalidateSessions(List.of(account.getIdOrThrow()));
    }

    @Override
    public void unassignAccount(Long permissionGroupId, Long accountId, Long actorAccountId) {
        PermissionGroup group = findActiveGroup(permissionGroupId);

        AccountGroupAssignment assignment = accountGroupAssignmentRepository
                .findByAccountIdAndPermissionGroupIdAndDeletedFalse(accountId, group.getIdOrThrow())
                .orElseThrow(() -> new IllegalArgumentException("활성화된 권한 그룹 할당이 없습니다."));

        assignment.softDelete(actorAccountId);
        accountGroupAssignmentRepository.save(assignment);
        sessionInvalidator.invalidateSessions(List.of(accountId));
    }

    private PermissionGroup findActiveGroup(Long id) {
        return permissionGroupRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new PermissionGroupNotFoundException("존재하지 않는 권한 그룹입니다: " + id));
    }

    private void ensureCustomGroup(PermissionGroup group) {
        if (group.isSystemGroup()) {
            throw new SystemPermissionGroupModificationDeniedException("시스템 권한 그룹은 수정하거나 삭제할 수 없습니다.");
        }
    }

    private void validateUniqueCustomGroupName(String name, @Nullable Long currentGroupId) {
        String normalizedName = name.trim();

        List<PermissionGroup> customGroups = permissionGroupRepository.findAllByDeletedFalse().stream()
                .filter(group -> group.getGroupType() == PermissionGroupType.CUSTOM)
                .toList();
        boolean exists = customGroups.stream()
                .anyMatch(group -> group.getName().equals(normalizedName)
                        && (currentGroupId == null || !group.getIdOrThrow().equals(currentGroupId)));
        if (exists) {
            throw new DuplicatePermissionGroupNameException("이미 존재하는 커스텀 권한 그룹 이름입니다: " + normalizedName);
        }
    }

    private List<Long> findActiveAccountIds(Long permissionGroupId) {
        return accountGroupAssignmentRepository.findAllByPermissionGroupIdAndDeletedFalse(permissionGroupId).stream()
                .map(AccountGroupAssignment::getAccountId)
                .distinct()
                .toList();
    }

    private void syncGrants(Long permissionGroupId, List<PermissionGroupGrantCommand> commands) {
        List<GroupPermissionGrant> existingGrants = groupPermissionGrantRepository.findAllByPermissionGroupId(permissionGroupId);
        Set<GrantKey> desired = resolveDesiredGrantKeys(commands);

        for (GroupPermissionGrant existingGrant : existingGrants) {
            GrantKey key = new GrantKey(
                    existingGrant.getPermissionId(),
                    existingGrant.getScope());
            if (desired.contains(key)) {
                if (existingGrant.isDeleted()) {
                    existingGrant.restore();
                    groupPermissionGrantRepository.save(existingGrant);
                }
                desired.remove(key);
                continue;
            }
            if (!existingGrant.isDeleted()) {
                existingGrant.softDelete(null);
                groupPermissionGrantRepository.save(existingGrant);
            }
        }

        for (GrantKey key : desired) {
            groupPermissionGrantRepository.save(GroupPermissionGrant.create(
                    permissionGroupId,
                    key.permissionId(),
                    key.scope()));
        }
    }

    private Set<GrantKey> resolveDesiredGrantKeys(List<PermissionGroupGrantCommand> commands) {
        Set<GrantKey> desired = new HashSet<>();

        for (PermissionGroupGrantCommand command : commands) {
            String permissionCode = command.permissionCode().trim();
            Permission permission = permissionRepository.findByCodeAndDeletedFalse(permissionCode)
                    .orElseThrow(() -> new PermissionNotFoundException(
                            "존재하지 않는 권한 코드입니다: " + command.permissionCode()));

            Set<PermissionScope> scopes = command.scopes();
            if (scopes == null || scopes.isEmpty()) {
                throw new IllegalArgumentException("권한 scope는 하나 이상 선택해야 합니다: " + command.permissionCode());
            }

            for (PermissionScope scope : scopes) {
                if (!PermissionScopePolicy.isScopeAllowed(permissionCode, scope)) {
                    throw new IllegalArgumentException(
                            "허용되지 않은 권한 scope 조합입니다: " + permissionCode + " -> " + scope.name());
                }
                GrantKey key = new GrantKey(permission.getIdOrThrow(), scope);
                if (!desired.add(key)) {
                    throw new IllegalArgumentException("중복된 권한 grant가 포함되어 있습니다: " + command.permissionCode());
                }
            }
        }

        return desired;
    }

    private record GrantKey(Long permissionId, PermissionScope scope) {
    }
}
