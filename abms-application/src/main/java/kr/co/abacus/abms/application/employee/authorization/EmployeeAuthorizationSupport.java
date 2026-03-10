package kr.co.abacus.abms.application.employee.authorization;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.account.Account;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.employee.Employee;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Component
class EmployeeAuthorizationSupport {

    private final AccountRepository accountRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    @Nullable
    EmployeeAuthorizationContext resolveContext(Long accountId) {
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            return null;
        }

        Employee employee = employeeRepository.findByIdAndDeletedFalse(account.getEmployeeId()).orElse(null);
        if (employee == null) {
            return null;
        }

        return new EmployeeAuthorizationContext(
                accountId,
                employee.getIdOrThrow(),
                employee.getDepartmentId()
        );
    }

    Set<Long> resolveDepartmentTree(Long rootDepartmentId) {
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
