package kr.co.abacus.abms.application.employee.authorization;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.permission.dto.GrantedPermissionDetail;
import kr.co.abacus.abms.application.permission.inbound.PermissionFinder;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class EmployeeReadAuthorizationService {

    public static final String EMPLOYEE_READ_PERMISSION_CODE = "employee.read";

    private final PermissionFinder permissionFinder;
    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public EmployeeReadScope resolveScope(Long accountId) {
        GrantedPermissionDetail permission = permissionFinder.findPermissions(accountId).permissions().stream()
                .filter(grantedPermission -> EMPLOYEE_READ_PERMISSION_CODE.equals(grantedPermission.code()))
                .findFirst()
                .orElse(null);
        if (permission == null) {
            return EmployeeReadScope.none();
        }

        Set<PermissionScope> scopes = permission.scopes();
        if (scopes.contains(PermissionScope.ALL)) {
            return EmployeeReadScope.all();
        }

        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            return EmployeeReadScope.none();
        }

        Employee employee = employeeRepository.findByIdAndDeletedFalse(account.getEmployeeId()).orElse(null);
        if (employee == null) {
            return EmployeeReadScope.none();
        }

        Long selfDepartmentId = employee.getDepartmentId();
        Set<Long> allowedDepartmentIds = new LinkedHashSet<>();
        if (scopes.contains(PermissionScope.OWN_DEPARTMENT)) {
            allowedDepartmentIds.add(selfDepartmentId);
        }
        if (scopes.contains(PermissionScope.OWN_DEPARTMENT_TREE)) {
            allowedDepartmentIds.addAll(resolveDepartmentTree(selfDepartmentId));
        }

        Long selfEmployeeId = scopes.contains(PermissionScope.SELF) ? employee.getIdOrThrow() : null;

        return new EmployeeReadScope(
                false,
                allowedDepartmentIds,
                selfEmployeeId,
                selfEmployeeId != null ? selfDepartmentId : null
        );
    }

    private Set<Long> resolveDepartmentTree(Long rootDepartmentId) {
        Map<Long, List<Long>> childrenByParentId = departmentRepository.findAllByDeletedFalse().stream()
                .filter(department -> department.getParent() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        department -> department.getParent().getIdOrThrow(),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.mapping(Department::getIdOrThrow, java.util.stream.Collectors.toList())
                ));

        Set<Long> allowedDepartmentIds = new LinkedHashSet<>();
        Queue<Long> queue = new ArrayDeque<>();
        queue.add(rootDepartmentId);

        while (!queue.isEmpty()) {
            Long departmentId = queue.poll();
            if (!allowedDepartmentIds.add(departmentId)) {
                continue;
            }
            queue.addAll(childrenByParentId.getOrDefault(departmentId, List.of()));
        }

        return allowedDepartmentIds;
    }
}
