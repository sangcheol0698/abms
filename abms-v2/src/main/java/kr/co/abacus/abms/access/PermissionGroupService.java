package kr.co.abacus.abms.access;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.account.AccountRepository;
import kr.co.abacus.abms.common.audit.AuditEventListener;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.NotFoundException;

/**
 * 권한 그룹 관리. 시스템 그룹은 권한 구성을 바꿀 수 없고 계정 할당만 가능하다.
 */
@Service
@Transactional
public class PermissionGroupService {

    private final PermissionGroupRepository groupRepository;
    private final PermissionRepository permissionRepository;
    private final GroupPermissionGrantRepository grantRepository;
    private final AccountGroupAssignmentRepository assignmentRepository;
    private final AccountRepository accountRepository;
    private final AuditEventListener auditLog;

    public PermissionGroupService(PermissionGroupRepository groupRepository, PermissionRepository permissionRepository,
                                  GroupPermissionGrantRepository grantRepository,
                                  AccountGroupAssignmentRepository assignmentRepository,
                                  AccountRepository accountRepository, AuditEventListener auditLog) {
        this.auditLog = auditLog;
        this.groupRepository = groupRepository;
        this.permissionRepository = permissionRepository;
        this.grantRepository = grantRepository;
        this.assignmentRepository = assignmentRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public List<PermissionGroup> groups() {
        return groupRepository.findAllByOrderByGroupTypeAscNameAsc();
    }

    @Transactional(readOnly = true)
    public PermissionGroup get(Long id) {
        return groupRepository.findById(id).orElseThrow(() -> NotFoundException.of("권한 그룹", id));
    }

    @Transactional(readOnly = true)
    public List<Permission> permissions() {
        return permissionRepository.findAllByOrderByCodeAsc();
    }

    @Transactional(readOnly = true)
    public long memberCount(Long groupId) {
        return assignmentRepository.countByPermissionGroupId(groupId);
    }

    /** 권한 ID → 부여된 범위 */
    @Transactional(readOnly = true)
    public Map<Long, Set<PermissionScope>> grants(Long groupId) {
        Map<Long, Set<PermissionScope>> result = new HashMap<>();
        for (GroupPermissionGrant grant : grantRepository.findAllByPermissionGroupId(groupId)) {
            result.computeIfAbsent(grant.getPermissionId(), k -> java.util.EnumSet.noneOf(PermissionScope.class)).add(grant.getScope());
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<Long> memberAccountIds(Long groupId) {
        return assignmentRepository.findAllByPermissionGroupId(groupId).stream().map(AccountGroupAssignment::getAccountId).toList();
    }

    @Transactional(readOnly = true)
    public List<Long> groupIdsOf(Long accountId) {
        return assignmentRepository.findAllByAccountId(accountId).stream().map(AccountGroupAssignment::getPermissionGroupId).toList();
    }

    public PermissionGroup create(String name, String description) {
        if (groupRepository.existsByName(name.trim())) {
            throw new BusinessException("이미 있는 그룹명입니다: " + name);
        }
        return groupRepository.save(PermissionGroup.custom(name, description));
    }

    /** 그룹 정보와 권한 구성을 한 번에 갱신한다. */
    public void update(Long groupId, String name, String description, Map<Long, Set<PermissionScope>> grants) {
        PermissionGroup group = get(groupId);
        group.requireCustom();
        if (groupRepository.existsByNameAndIdNot(name.trim(), groupId)) {
            throw new BusinessException("이미 있는 그룹명입니다: " + name);
        }
        group.update(name, description);

        Map<Long, Permission> permissions = permissionRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(Permission::id, java.util.function.Function.identity()));
        Set<Long> validPermissionIds = permissions.keySet();
        String before = describeGrants(grantRepository.findAllByPermissionGroupId(groupId).stream()
                .collect(java.util.stream.Collectors.groupingBy(GroupPermissionGrant::getPermissionId,
                        java.util.stream.Collectors.mapping(GroupPermissionGrant::getScope, java.util.stream.Collectors.toSet()))), permissions);
        grantRepository.deleteAllByGroupId(groupId);
        grantRepository.flush();
        grants.forEach((permissionId, scopes) -> {
            if (!validPermissionIds.contains(permissionId)) {
                throw NotFoundException.of("권한", permissionId);
            }
            scopes.forEach(scope -> grantRepository.save(GroupPermissionGrant.of(groupId, permissionId, scope)));
        });
        // 권한 구성은 삭제 후 재생성하므로 행 단위가 아니라 그룹의 한 속성으로 이력을 남긴다.
        auditLog.record(group, groupId, "grants", before, describeGrants(grants, permissions));
    }

    /** 권한 구성 요약: "직원 조회(전체), 프로젝트 조회(부서 트리·참여)" — 권한명 순 */
    private static String describeGrants(Map<Long, Set<PermissionScope>> grants, Map<Long, Permission> permissions) {
        return grants.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty() && permissions.containsKey(e.getKey()))
                .map(e -> permissions.get(e.getKey()).getName() + "(" + e.getValue().stream().sorted()
                        .map(PermissionScope::label).collect(java.util.stream.Collectors.joining("·")) + ")")
                .sorted()
                .collect(java.util.stream.Collectors.joining(", "));
    }

    public void delete(Long groupId) {
        PermissionGroup group = get(groupId);
        group.requireCustom();
        grantRepository.deleteAllByGroupId(groupId);
        assignmentRepository.deleteAllByGroupId(groupId);
        groupRepository.delete(group);
    }

    public void addMember(Long groupId, Long accountId) {
        get(groupId);
        accountRepository.findById(accountId).orElseThrow(() -> NotFoundException.of("계정", accountId));
        if (assignmentRepository.existsByAccountIdAndPermissionGroupId(accountId, groupId)) {
            throw new BusinessException("이미 그룹에 속한 계정입니다.");
        }
        assignmentRepository.save(AccountGroupAssignment.of(accountId, groupId));
    }

    public void removeMember(Long groupId, Long accountId, Long actorAccountId) {
        if (groupId == PermissionGroup.ADMIN_GROUP_ID && accountId.equals(actorAccountId)) {
            throw new BusinessException("본인을 최고 관리자 그룹에서 제외할 수 없습니다.");
        }
        AccountGroupAssignment assignment = assignmentRepository.findByAccountIdAndPermissionGroupId(accountId, groupId)
                .orElseThrow(() -> new BusinessException("그룹에 속하지 않은 계정입니다."));
        assignmentRepository.delete(assignment);
    }

}
