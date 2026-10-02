package kr.co.abacus.abms.security;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.access.PermissionScope;
import kr.co.abacus.abms.department.DepartmentRepository;
import kr.co.abacus.abms.department.DepartmentTree;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.project.ProjectAssignmentRepository;

/**
 * 권한 코드 + 범위(scope)를 실제 데이터 접근 범위로 해석한다.
 */
@Service
@Transactional(readOnly = true)
public class AccessService {

    private final DepartmentRepository departmentRepository;
    private final ProjectAssignmentRepository assignmentRepository;

    public AccessService(DepartmentRepository departmentRepository, ProjectAssignmentRepository assignmentRepository) {
        this.departmentRepository = departmentRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public DataScope scopeOf(LoginUser user, PermissionCode code) {
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
            DepartmentTree tree = new DepartmentTree(departmentRepository.findAll());
            departmentIds.addAll(tree.subtreeIds(user.departmentId()));
        }
        if (scopes.contains(PermissionScope.OWN_DEPARTMENT)) {
            departmentIds.add(user.departmentId());
        }
        if (scopes.contains(PermissionScope.SELF)) {
            employeeIds.add(user.employeeId());
        }
        if (scopes.contains(PermissionScope.CURRENT_PARTICIPATION) || scopes.contains(PermissionScope.SELF)) {
            projectIds.addAll(assignmentRepository.findActiveProjectIds(user.employeeId(), LocalDate.now()));
        }
        return new DataScope(false, Set.copyOf(departmentIds), Set.copyOf(employeeIds), Set.copyOf(projectIds));
    }

    public boolean canAccessEmployee(LoginUser user, PermissionCode code, Employee employee) {
        return scopeOf(user, code).coversEmployee(employee.id(), employee.getDepartmentId());
    }

    public void checkEmployee(LoginUser user, PermissionCode code, Employee employee) {
        if (!canAccessEmployee(user, code, employee)) {
            throw new AccessDeniedException("해당 직원에 대한 권한이 없습니다.");
        }
    }

    public void checkDepartment(LoginUser user, PermissionCode code, Long departmentId) {
        if (!scopeOf(user, code).coversDepartment(departmentId)) {
            throw new AccessDeniedException("해당 부서에 대한 권한이 없습니다.");
        }
    }

    public boolean canAccessProject(LoginUser user, PermissionCode code, Project project) {
        return scopeOf(user, code).coversProject(project.id(), project.getLeadDepartmentId());
    }

    public void checkProject(LoginUser user, PermissionCode code, Project project) {
        if (!canAccessProject(user, code, project)) {
            throw new AccessDeniedException("해당 프로젝트에 대한 권한이 없습니다.");
        }
    }

    public void require(LoginUser user, PermissionCode code) {
        if (!user.has(code)) {
            throw new AccessDeniedException("권한이 없습니다: " + code.code());
        }
    }

}
