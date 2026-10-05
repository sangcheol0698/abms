package kr.co.abacus.abms.security;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.access.AccountGroupAssignment;
import kr.co.abacus.abms.access.AccountGroupAssignmentRepository;
import kr.co.abacus.abms.access.GroupPermissionGrant;
import kr.co.abacus.abms.access.GroupPermissionGrantRepository;
import kr.co.abacus.abms.access.Permission;
import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.access.PermissionRepository;
import kr.co.abacus.abms.access.PermissionScope;
import kr.co.abacus.abms.account.Account;
import kr.co.abacus.abms.account.AccountRepository;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeRepository;

@Service
@Transactional(readOnly = true)
public class LoginUserService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final AccountGroupAssignmentRepository assignmentRepository;
    private final GroupPermissionGrantRepository grantRepository;
    private final PermissionRepository permissionRepository;

    public LoginUserService(AccountRepository accountRepository, EmployeeRepository employeeRepository,
                            AccountGroupAssignmentRepository assignmentRepository,
                            GroupPermissionGrantRepository grantRepository,
                            PermissionRepository permissionRepository) {
        this.accountRepository = accountRepository;
        this.employeeRepository = employeeRepository;
        this.assignmentRepository = assignmentRepository;
        this.grantRepository = grantRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public LoginUser loadUserByUsername(String username) {
        Account account = accountRepository.findByUsername(username.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("계정을 찾을 수 없습니다."));
        Employee employee = employeeRepository.findByIdAndDeletedFalse(account.getEmployeeId())
                .orElseThrow(() -> new UsernameNotFoundException("직원 정보를 찾을 수 없습니다."));

        boolean enabled = account.isEnabled() && !employee.isResigned();
        return new LoginUser(
                account.id(),
                employee.id(),
                employee.getDepartmentId(),
                account.getUsername(),
                employee.getName(),
                employee.photoUrl(),
                account.getPassword(),
                enabled,
                account.isLocked(),
                loadGrants(account.id()));
    }

    public Map<PermissionCode, Set<PermissionScope>> loadGrants(Long accountId) {
        List<Long> groupIds = assignmentRepository.findAllByAccountId(accountId).stream()
                .map(AccountGroupAssignment::getPermissionGroupId)
                .toList();
        if (groupIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Permission> permissions = permissionRepository.findAll().stream()
                .collect(Collectors.toMap(Permission::id, Function.identity()));

        Map<PermissionCode, Set<PermissionScope>> grants = new EnumMap<>(PermissionCode.class);
        for (GroupPermissionGrant grant : grantRepository.findAllByPermissionGroupIdIn(groupIds)) {
            Permission permission = permissions.get(grant.getPermissionId());
            if (permission == null) {
                continue;
            }
            PermissionCode.fromCode(permission.getCode()).ifPresent(code ->
                    grants.computeIfAbsent(code, k -> EnumSet.noneOf(PermissionScope.class)).add(grant.getScope()));
        }
        return grants;
    }

}
