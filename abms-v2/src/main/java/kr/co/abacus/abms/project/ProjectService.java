package kr.co.abacus.abms.project;

import java.time.Year;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.NotFoundException;
import kr.co.abacus.abms.department.DepartmentRepository;
import kr.co.abacus.abms.party.PartyRepository;
import kr.co.abacus.abms.project.Project.ProjectInfo;
import kr.co.abacus.abms.security.AccessService;
import kr.co.abacus.abms.security.DataScope;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 프로젝트 관리 유스케이스. 조회/변경은 권한 범위(주관 부서, 참여 프로젝트) 안에서만 가능하다.
 */
@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectRevenuePlanRepository revenuePlanRepository;
    private final ProjectAssignmentRepository assignmentRepository;
    private final PartyRepository partyRepository;
    private final DepartmentRepository departmentRepository;
    private final AccessService accessService;

    public ProjectService(ProjectRepository projectRepository, ProjectRevenuePlanRepository revenuePlanRepository,
                          ProjectAssignmentRepository assignmentRepository, PartyRepository partyRepository,
                          DepartmentRepository departmentRepository, AccessService accessService) {
        this.projectRepository = projectRepository;
        this.revenuePlanRepository = revenuePlanRepository;
        this.assignmentRepository = assignmentRepository;
        this.partyRepository = partyRepository;
        this.departmentRepository = departmentRepository;
        this.accessService = accessService;
    }

    @Transactional(readOnly = true)
    public Page<Project> search(LoginUser user, ProjectSearch search, Pageable pageable) {
        DataScope scope = accessService.scopeOf(user, PermissionCode.PROJECT_READ);
        if (scope.isNone()) {
            return Page.empty(pageable);
        }
        return projectRepository.findAll(search.toSpecification(scope), pageable);
    }

    @Transactional(readOnly = true)
    public List<Project> exportable(LoginUser user, ProjectSearch search) {
        DataScope readScope = accessService.scopeOf(user, PermissionCode.PROJECT_READ);
        if (!user.has(PermissionCode.PROJECT_EXPORT) || readScope.isNone()) {
            throw new AccessDeniedException("프로젝트 내보내기 권한이 없습니다.");
        }
        return projectRepository.findAll(search.toSpecification(readScope));
    }

    @Transactional(readOnly = true)
    public Project get(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> NotFoundException.of("프로젝트", id));
    }

    @Transactional(readOnly = true)
    public Project getForRead(LoginUser user, Long id) {
        Project project = get(id);
        accessService.checkProject(user, PermissionCode.PROJECT_READ, project);
        return project;
    }

    @Transactional(readOnly = true)
    public Project getForWrite(LoginUser user, Long id) {
        Project project = get(id);
        accessService.checkProject(user, PermissionCode.PROJECT_WRITE, project);
        return project;
    }

    @Transactional(readOnly = true)
    public boolean canRead(LoginUser user, Project project) {
        return accessService.canAccessProject(user, PermissionCode.PROJECT_READ, project);
    }

    @Transactional(readOnly = true)
    public boolean canWrite(LoginUser user, Project project) {
        return accessService.canAccessProject(user, PermissionCode.PROJECT_WRITE, project);
    }

    @Transactional(readOnly = true)
    public List<Project> byParty(Long partyId) {
        return projectRepository.findAllByPartyIdOrderByPeriodStartDateDesc(partyId);
    }

    @Transactional(readOnly = true)
    public String suggestCode() {
        String prefix = "PRJ-" + Year.now().getValue() + "-";
        long next = projectRepository.countByCodePrefix(prefix) + 1;
        String code;
        do {
            code = prefix + String.format("%03d", next++);
        } while (projectRepository.existsByCode(code));
        return code;
    }

    public Project create(LoginUser user, String code, ProjectInfo info) {
        checkWriteForDepartment(user, info.leadDepartmentId());
        validateReferences(info);
        if (projectRepository.existsByCode(code.trim())) {
            throw new BusinessException("이미 사용 중인 프로젝트 코드입니다: " + code);
        }
        return projectRepository.save(Project.create(code, info));
    }

    public void update(LoginUser user, Long id, ProjectInfo info) {
        Project project = getForWrite(user, id);
        if (!project.getLeadDepartmentId().equals(info.leadDepartmentId())) {
            checkWriteForDepartment(user, info.leadDepartmentId());
        }
        validateReferences(info);
        project.update(info);
    }

    public void complete(LoginUser user, Long id) {
        getForWrite(user, id).complete();
    }

    public void cancel(LoginUser user, Long id) {
        getForWrite(user, id).cancel();
    }

    /** 프로젝트와 하위 매출 계획/투입을 함께 삭제한다. 손익 집계는 다음 재집계 시 제거된다. */
    public void delete(LoginUser user, Long id) {
        Project project = getForWrite(user, id);
        revenuePlanRepository.findAllByProjectIdOrderBySequenceAsc(id).forEach(plan -> plan.softDelete(user.accountId()));
        assignmentRepository.findAllByProjectIdOrderByPeriodStartDateAsc(id).forEach(a -> a.softDelete(user.accountId()));
        project.softDelete(user.accountId());
    }

    private void checkWriteForDepartment(LoginUser user, Long departmentId) {
        if (!accessService.scopeOf(user, PermissionCode.PROJECT_WRITE).coversDepartment(departmentId)) {
            throw new AccessDeniedException("해당 부서 주관 프로젝트를 관리할 권한이 없습니다.");
        }
    }

    private void validateReferences(ProjectInfo info) {
        if (!partyRepository.existsById(info.partyId())) {
            throw NotFoundException.of("협력사", info.partyId());
        }
        if (!departmentRepository.existsById(info.leadDepartmentId())) {
            throw NotFoundException.of("부서", info.leadDepartmentId());
        }
    }

}
