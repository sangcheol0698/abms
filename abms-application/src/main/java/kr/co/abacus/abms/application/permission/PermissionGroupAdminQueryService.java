package kr.co.abacus.abms.application.permission;

import static org.springframework.util.StringUtils.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.permission.dto.PermissionCatalogItem;
import kr.co.abacus.abms.application.permission.dto.PermissionGroupAccountSummary;
import kr.co.abacus.abms.application.permission.dto.PermissionGroupCatalog;
import kr.co.abacus.abms.application.permission.dto.PermissionGroupDetail;
import kr.co.abacus.abms.application.permission.dto.PermissionGroupGrantDetail;
import kr.co.abacus.abms.application.permission.dto.PermissionGroupSummary;
import kr.co.abacus.abms.application.permission.inbound.PermissionGroupAdminFinder;
import kr.co.abacus.abms.application.permission.outbound.AccountGroupAssignmentRepository;
import kr.co.abacus.abms.application.permission.outbound.GroupPermissionGrantRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionGroupRepository;
import kr.co.abacus.abms.application.permission.outbound.PermissionRepository;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.accountgroupassignment.AccountGroupAssignment;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.grouppermissiongrant.GroupPermissionGrant;
import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;
import kr.co.abacus.abms.domain.permission.Permission;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroup;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupNotFoundException;
import kr.co.abacus.abms.domain.permissiongroup.PermissionGroupType;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PermissionGroupAdminQueryService implements PermissionGroupAdminFinder {

    private static final Comparator<PermissionScope> SCOPE_ORDER =
            Comparator.comparingInt(PermissionScope::getOrder);
    private static final Comparator<PermissionGroup> GROUP_ORDER = Comparator
            .comparingInt((PermissionGroup group) -> group.isSystemGroup() ? 0 : 1)
            .thenComparing(PermissionGroup::getName, String.CASE_INSENSITIVE_ORDER);

    private final PermissionGroupRepository permissionGroupRepository;
    private final AccountGroupAssignmentRepository accountGroupAssignmentRepository;
    private final GroupPermissionGrantRepository groupPermissionGrantRepository;
    private final PermissionRepository permissionRepository;
    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public List<PermissionGroupSummary> searchGroups(@Nullable String name, @Nullable PermissionGroupType groupType) {
        List<PermissionGroup> groups = permissionGroupRepository.findAllByDeletedFalse().stream()
                .filter(group -> matchesGroupType(group, groupType))
                .filter(group -> matchesName(group, name))
                .sorted(GROUP_ORDER)
                .toList();
        if (groups.isEmpty()) {
            return List.of();
        }

        List<Long> groupIds = groups.stream()
                .map(PermissionGroup::getIdOrThrow)
                .toList();

        Map<Long, Integer> assignmentCounts = countByGroupId(
                accountGroupAssignmentRepository.findAllByPermissionGroupIdInAndDeletedFalse(groupIds).stream()
                        .map(AccountGroupAssignment::getPermissionGroupId)
                        .toList());
        Map<Long, Integer> grantCounts = countByGroupId(
                groupPermissionGrantRepository.findAllByPermissionGroupIdInAndDeletedFalse(groupIds).stream()
                        .map(GroupPermissionGrant::getPermissionGroupId)
                        .toList());

        return groups.stream()
                .map(group -> new PermissionGroupSummary(
                        group.getIdOrThrow(),
                        group.getName(),
                        group.getDescription(),
                        group.getGroupType(),
                        assignmentCounts.getOrDefault(group.getIdOrThrow(), 0),
                        grantCounts.getOrDefault(group.getIdOrThrow(), 0)))
                .toList();
    }

    @Override
    public PermissionGroupDetail findGroupDetail(Long id) {
        PermissionGroup group = permissionGroupRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new PermissionGroupNotFoundException("존재하지 않는 권한 그룹입니다: " + id));

        List<PermissionGroupGrantDetail> grantDetails = resolveGrantDetails(group.getIdOrThrow());
        List<PermissionGroupAccountSummary> accounts = resolveAssignedAccounts(group.getIdOrThrow());

        return new PermissionGroupDetail(
                group.getIdOrThrow(),
                group.getName(),
                group.getDescription(),
                group.getGroupType(),
                grantDetails,
                accounts);
    }

    @Override
    public PermissionGroupCatalog getCatalog() {
        List<PermissionCatalogItem> permissions = permissionRepository.findAllByDeletedFalseOrderByCodeAsc().stream()
                .map(permission -> new PermissionCatalogItem(
                        permission.getCode(),
                        permission.getName(),
                        permission.getDescription(),
                        PermissionScopePolicy.allowedScopesFor(permission.getCode()).stream()
                                .sorted(SCOPE_ORDER)
                                .toList()))
                .toList();

        List<PermissionScope> scopes = java.util.Arrays.stream(PermissionScope.values())
                .sorted(SCOPE_ORDER)
                .toList();

        return new PermissionGroupCatalog(permissions, scopes);
    }

    @Override
    public List<PermissionGroupAccountSummary> searchAssignableAccounts(@Nullable String keyword, Long permissionGroupId) {
        PermissionGroup group = permissionGroupRepository.findByIdAndDeletedFalse(permissionGroupId)
                .orElseThrow(() -> new PermissionGroupNotFoundException(
                        "존재하지 않는 권한 그룹입니다: " + permissionGroupId));

        Set<Long> assignedAccountIds = accountGroupAssignmentRepository
                .findAllByPermissionGroupIdAndDeletedFalse(group.getIdOrThrow()).stream()
                .map(AccountGroupAssignment::getAccountId)
                .collect(Collectors.toSet());

        return resolveAccountSummaries(accountRepository.findAllByDeletedFalse()).stream()
                .filter(account -> !assignedAccountIds.contains(account.accountId()))
                .filter(account -> matchesKeyword(account, keyword))
                .toList();
    }

    private List<PermissionGroupGrantDetail> resolveGrantDetails(Long permissionGroupId) {
        List<GroupPermissionGrant> grants = groupPermissionGrantRepository.findAllByPermissionGroupIdAndDeletedFalse(
                permissionGroupId);
        if (grants.isEmpty()) {
            return List.of();
        }

        List<Long> permissionIds = grants.stream()
                .map(GroupPermissionGrant::getPermissionId)
                .distinct()
                .toList();

        Map<Long, Permission> permissionsById = permissionRepository.findAllByIdInAndDeletedFalse(permissionIds).stream()
                .collect(Collectors.toMap(Permission::getIdOrThrow, Function.identity()));

        return grants.stream()
                .filter(grant -> permissionsById.containsKey(grant.getPermissionId()))
                .collect(Collectors.groupingBy(
                        GroupPermissionGrant::getPermissionId,
                        Collectors.mapping(
                                GroupPermissionGrant::getScope,
                                Collectors.toCollection(LinkedHashSet::new))))
                .entrySet().stream()
                .map(entry -> {
                    Permission permission = permissionsById.get(entry.getKey());
                    if (permission == null) {
                        throw new IllegalStateException("권한 정보를 찾을 수 없습니다: " + entry.getKey());
                    }
                    return new PermissionGroupGrantDetail(
                            permission.getCode(),
                            permission.getName(),
                            permission.getDescription(),
                            sortScopes(entry.getValue()));
                })
                .sorted(Comparator.comparing(PermissionGroupGrantDetail::permissionCode))
                .toList();
    }

    private List<PermissionGroupAccountSummary> resolveAssignedAccounts(Long permissionGroupId) {
        List<AccountGroupAssignment> assignments = accountGroupAssignmentRepository
                .findAllByPermissionGroupIdAndDeletedFalse(permissionGroupId);
        if (assignments.isEmpty()) {
            return List.of();
        }

        List<Long> accountIds = assignments.stream()
                .map(AccountGroupAssignment::getAccountId)
                .distinct()
                .toList();

        return resolveAccountSummaries(accountRepository.findAllByIdInAndDeletedFalse(accountIds));
    }

    private List<PermissionGroupAccountSummary> resolveAccountSummaries(List<Account> accounts) {
        if (accounts.isEmpty()) {
            return List.of();
        }

        Map<Long, Employee> employeesById = employeeRepository.findAllByIdInAndDeletedFalse(
                        accounts.stream().map(Account::getEmployeeId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(Employee::getIdOrThrow, Function.identity()));
        Map<Long, Department> departmentsById = departmentRepository.findAllByDeletedFalse().stream()
                .collect(Collectors.toMap(Department::getIdOrThrow, Function.identity()));

        List<PermissionGroupAccountSummary> summaries = new ArrayList<>();
        for (Account account : accounts) {
            Employee employee = employeesById.get(account.getEmployeeId());
            if (employee == null) {
                continue;
            }
            Department department = departmentsById.get(employee.getDepartmentId());
            summaries.add(new PermissionGroupAccountSummary(
                    account.getIdOrThrow(),
                    employee.getIdOrThrow(),
                    employee.getName(),
                    account.getUsername().address(),
                    department != null ? department.getName() : null,
                    employee.getPosition()));
        }

        return summaries.stream()
                .sorted(Comparator.comparing(PermissionGroupAccountSummary::employeeName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(PermissionGroupAccountSummary::email, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private boolean matchesGroupType(PermissionGroup group, @Nullable PermissionGroupType groupType) {
        return groupType == null || group.getGroupType() == groupType;
    }

    private boolean matchesName(PermissionGroup group, @Nullable String name) {
        return !hasText(name) || group.getName().toLowerCase().contains(name.trim().toLowerCase());
    }

    private boolean matchesKeyword(PermissionGroupAccountSummary account, @Nullable String keyword) {
        if (!hasText(keyword)) {
            return true;
        }
        String normalized = keyword.trim().toLowerCase();
        return account.employeeName().toLowerCase().contains(normalized)
                || account.email().toLowerCase().contains(normalized);
    }

    private Map<Long, Integer> countByGroupId(List<Long> groupIds) {
        return groupIds.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        id -> 1,
                        Integer::sum));
    }

    private Set<PermissionScope> sortScopes(Set<PermissionScope> scopes) {
        return scopes.stream()
                .sorted(SCOPE_ORDER)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
