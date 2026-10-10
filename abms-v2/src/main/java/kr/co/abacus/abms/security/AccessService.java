package kr.co.abacus.abms.security;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.access.PermissionScope;

/**
 * 권한 코드 + 범위(scope)를 실제 데이터 접근 범위로 해석한다.
 */
@Service
@Transactional(readOnly = true)
public class AccessService {

    private final DepartmentHierarchy departmentHierarchy;
    private final ParticipationLookup participationLookup;
    private final ObjectProvider<DataScopeCache> scopeCache;

    public AccessService(DepartmentHierarchy departmentHierarchy, ParticipationLookup participationLookup,
                         ObjectProvider<DataScopeCache> scopeCache) {
        this.departmentHierarchy = departmentHierarchy;
        this.participationLookup = participationLookup;
        this.scopeCache = scopeCache;
    }

    /**
     * 사용자의 권한 범위. 웹 요청 안에서는 (사용자, 권한 코드)별로 한 번만 계산해 {@link DataScopeCache} 에 보관한다.
     * 목록을 권한으로 걸러낼 때 항목마다 부서 트리·참여 프로젝트를 다시 조회하지 않기 위해서다.
     */
    public DataScope scopeOf(LoginUser user, PermissionCode code) {
        DataScopeCache cache = scopeCache.getIfAvailable();
        if (cache == null) {
            return computeScope(user, code);
        }
        return cache.get(user.accountId() + "." + code.name(), () -> computeScope(user, code));
    }

    private DataScope computeScope(LoginUser user, PermissionCode code) {
        Set<PermissionScope> scopes = user.scopes(code);
        if (scopes.isEmpty()) {
            return DataScope.NONE;
        }
        if (scopes.contains(PermissionScope.ALL)) {
            return DataScope.ALL;
        }
        Set<Long> departmentIds = new HashSet<>();
        Set<Long> employeeIds = new HashSet<>();
        Set<Long> projectIds = new HashSet<>();
        if (scopes.contains(PermissionScope.OWN_DEPARTMENT_TREE)) {
            departmentIds.addAll(departmentHierarchy.subtreeIds(user.departmentId()));
        }
        if (scopes.contains(PermissionScope.OWN_DEPARTMENT)) {
            departmentIds.add(user.departmentId());
        }
        if (scopes.contains(PermissionScope.SELF)) {
            employeeIds.add(user.employeeId());
        }
        if (scopes.contains(PermissionScope.CURRENT_PARTICIPATION) || scopes.contains(PermissionScope.SELF)) {
            projectIds.addAll(participationLookup.findActiveProjectIds(user.employeeId(), LocalDate.now()));
        }
        return new DataScope(false, Set.copyOf(departmentIds), Set.copyOf(employeeIds), Set.copyOf(projectIds));
    }

    public boolean canAccessEmployee(LoginUser user, PermissionCode code, ScopedEmployee employee) {
        return scopeOf(user, code).coversEmployee(employee.id(), employee.getDepartmentId());
    }

    public void checkEmployee(LoginUser user, PermissionCode code, ScopedEmployee employee) {
        if (!canAccessEmployee(user, code, employee)) {
            throw new AccessDeniedException("해당 직원에 대한 권한이 없습니다.");
        }
    }

    public void checkDepartment(LoginUser user, PermissionCode code, Long departmentId) {
        if (!scopeOf(user, code).coversDepartment(departmentId)) {
            throw new AccessDeniedException("해당 부서에 대한 권한이 없습니다.");
        }
    }

    public boolean canAccessProject(LoginUser user, PermissionCode code, ScopedProject project) {
        return scopeOf(user, code).coversProject(project.id(), project.getLeadDepartmentId());
    }

    public void checkProject(LoginUser user, PermissionCode code, ScopedProject project) {
        if (!canAccessProject(user, code, project)) {
            throw new AccessDeniedException("해당 프로젝트에 대한 권한이 없습니다.");
        }
    }

    public void require(LoginUser user, PermissionCode code) {
        if (!user.has(code)) {
            throw new AccessDeniedException("'" + code.label() + "' 권한이 없습니다.");
        }
    }

}
