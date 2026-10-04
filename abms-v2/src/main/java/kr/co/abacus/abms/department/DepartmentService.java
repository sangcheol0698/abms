package kr.co.abacus.abms.department;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.NotFoundException;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeRepository;
import kr.co.abacus.abms.project.ProjectRepository;
import kr.co.abacus.abms.security.AccessService;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.site.SiteRepository;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final ProjectRepository projectRepository;
    private final AccessService accessService;
    private final SiteRepository siteRepository;

    public DepartmentService(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository,
                             ProjectRepository projectRepository, AccessService accessService, SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.projectRepository = projectRepository;
        this.accessService = accessService;
    }

    @Transactional(readOnly = true)
    public DepartmentTree tree() {
        return new DepartmentTree(departmentRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Department get(Long id) {
        return departmentRepository.findById(id).orElseThrow(() -> NotFoundException.of("부서", id));
    }

    @Transactional(readOnly = true)
    public List<Employee> members(Long departmentId) {
        return employeeRepository.findAllByDepartmentIdAndDeletedFalse(departmentId).stream()
                .sorted(java.util.Comparator.comparingInt((Employee e) -> e.getPosition().level()).reversed()
                        .thenComparing(Employee::getName))
                .toList();
    }

    public Department create(LoginUser user, String code, String name, DepartmentType type, @Nullable Long parentId,
                             @Nullable String description, @Nullable Long siteId) {
        accessService.require(user, PermissionCode.DEPARTMENT_WRITE);
        if (departmentRepository.existsByCode(code.trim())) {
            throw new BusinessException("이미 사용 중인 부서 코드입니다: " + code);
        }
        if (parentId != null) {
            get(parentId);
            accessService.checkDepartment(user, PermissionCode.DEPARTMENT_WRITE, parentId);
        } else if (!user.scopes(PermissionCode.DEPARTMENT_WRITE).contains(kr.co.abacus.abms.access.PermissionScope.ALL)) {
            throw new org.springframework.security.access.AccessDeniedException("최상위 부서는 전체 권한이 있어야 생성할 수 있습니다.");
        }
        Department department = Department.create(code, name, type, parentId);
        department.describe(description);
        department.relocate(requireSite(siteId));
        return departmentRepository.save(department);
    }

    public void update(LoginUser user, Long id, String name, DepartmentType type, @Nullable Long parentId, @Nullable String description,
                       @Nullable Long siteId) {
        Department department = get(id);
        accessService.checkDepartment(user, PermissionCode.DEPARTMENT_WRITE, id);
        if (parentId != null && tree().subtreeIds(id).contains(parentId)) {
            throw new BusinessException("하위 부서를 상위 부서로 지정할 수 없습니다.");
        }
        department.update(name, type, parentId);
        department.describe(description);
        department.relocate(requireSite(siteId));
    }

    private @Nullable Long requireSite(@Nullable Long siteId) {
        if (siteId != null && !siteRepository.existsById(siteId)) {
            throw NotFoundException.of("사업장", siteId);
        }
        return siteId;
    }

    public void assignLeader(LoginUser user, Long id, @Nullable Long leaderEmployeeId) {
        Department department = get(id);
        accessService.checkDepartment(user, PermissionCode.DEPARTMENT_WRITE, id);
        if (leaderEmployeeId != null) {
            Employee leader = employeeRepository.findByIdAndDeletedFalse(leaderEmployeeId)
                    .orElseThrow(() -> NotFoundException.of("직원", leaderEmployeeId));
            if (leader.isResigned()) {
                throw new BusinessException("퇴사한 직원은 부서장으로 지정할 수 없습니다.");
            }
        }
        department.assignLeader(leaderEmployeeId);
    }

    public void delete(LoginUser user, Long id) {
        Department department = get(id);
        accessService.checkDepartment(user, PermissionCode.DEPARTMENT_WRITE, id);
        if (!departmentRepository.findAllByParentId(id).isEmpty()) {
            throw new BusinessException("하위 부서가 있는 부서는 삭제할 수 없습니다.");
        }
        if (!members(id).isEmpty()) {
            throw new BusinessException("소속 직원이 있는 부서는 삭제할 수 없습니다.");
        }
        if (!projectRepository.findAllByLeadDepartmentIdInOrderByPeriodStartDateDesc(List.of(id)).isEmpty()) {
            throw new BusinessException("주관 프로젝트가 있는 부서는 삭제할 수 없습니다.");
        }
        department.softDelete(user.accountId());
    }

}
