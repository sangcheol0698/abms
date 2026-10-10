package kr.co.abacus.abms.project;

import java.time.LocalDate;
import java.time.Year;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.jspecify.annotations.Nullable;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Location;
import kr.co.abacus.abms.common.domain.NotFoundException;
import kr.co.abacus.abms.department.DepartmentDeleting;
import kr.co.abacus.abms.department.DepartmentRepository;
import kr.co.abacus.abms.party.PartyDeleting;
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
    private final ProjectExpenseRepository expenseRepository;
    private final PartyRepository partyRepository;
    private final DepartmentRepository departmentRepository;
    private final AccessService accessService;

    public ProjectService(ProjectRepository projectRepository, ProjectRevenuePlanRepository revenuePlanRepository,
                          ProjectAssignmentRepository assignmentRepository, ProjectExpenseRepository expenseRepository,
                          PartyRepository partyRepository, DepartmentRepository departmentRepository, AccessService accessService) {
        this.projectRepository = projectRepository;
        this.revenuePlanRepository = revenuePlanRepository;
        this.assignmentRepository = assignmentRepository;
        this.expenseRepository = expenseRepository;
        this.partyRepository = partyRepository;
        this.departmentRepository = departmentRepository;
        this.accessService = accessService;
    }

    /** 협력사의 프로젝트 수 */
    @Transactional(readOnly = true)
    public long partyProjectCount(Long partyId) {
        return projectRepository.countByPartyId(partyId);
    }

    /** 협력사별 프로젝트 수 (한 번의 집계 쿼리) */
    @Transactional(readOnly = true)
    public Map<Long, Long> partyProjectCounts(Collection<Long> partyIds) {
        if (partyIds.isEmpty()) {
            return Map.of();
        }
        return projectRepository.countGroupByPartyId(partyIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));
    }

    /** 주관 프로젝트가 있는 부서는 삭제할 수 없다. */
    @EventListener
    @Order(2)
    public void onDepartmentDeleting(DepartmentDeleting event) {
        if (!projectRepository.findAllByLeadDepartmentIdInOrderByPeriodStartDateDesc(List.of(event.departmentId())).isEmpty()) {
            throw new BusinessException("주관 프로젝트가 있는 부서는 삭제할 수 없습니다.");
        }
    }

    /** 프로젝트가 연결된 협력사는 삭제할 수 없다. */
    @EventListener
    public void onPartyDeleting(PartyDeleting event) {
        if (projectRepository.existsByPartyId(event.partyId())) {
            throw new BusinessException("프로젝트가 연결된 협력사는 삭제할 수 없습니다.");
        }
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
        return create(user, code, info, null, null);
    }

    public Project create(LoginUser user, String code, ProjectInfo info, @Nullable WorkPlace workPlace, @Nullable Location workLocation) {
        checkWriteForDepartment(user, info.leadDepartmentId());
        validateReferences(info);
        if (projectRepository.existsByCode(code.trim())) {
            throw new BusinessException("이미 사용 중인 프로젝트 코드입니다: " + code);
        }
        Project project = Project.create(code, info);
        project.assignWorkPlace(workPlace, workLocation);
        return projectRepository.save(project);
    }

    public void update(LoginUser user, Long id, ProjectInfo info) {
        Project project = getForWrite(user, id);
        update(user, id, info, project.getWorkPlace(), project.getWorkLocation());
    }

    public void update(LoginUser user, Long id, ProjectInfo info, @Nullable WorkPlace workPlace, @Nullable Location workLocation) {
        Project project = getForWrite(user, id);
        if (!project.getLeadDepartmentId().equals(info.leadDepartmentId())) {
            checkWriteForDepartment(user, info.leadDepartmentId());
        }
        validateReferences(info);
        project.update(info);
        project.assignWorkPlace(workPlace, workLocation);
    }

    public void complete(LoginUser user, Long id) {
        getForWrite(user, id).complete();
    }

    public void cancel(LoginUser user, Long id) {
        getForWrite(user, id).cancel();
    }

    /**
     * 프로젝트와 하위 매출 계획/투입을 함께 삭제한다. 손익 집계는 다음 재집계 시 제거된다.
     * 삭제하면 실적이 집계에서 사라지므로, 발행된 매출이나 이미 시작된 투입, 직접비가 있으면 삭제 대신 취소 처리해야 한다.
     */
    public void delete(LoginUser user, Long id) {
        Project project = getForWrite(user, id);
        if (revenuePlanRepository.existsByProjectIdAndIssuedTrue(id)) {
            throw new BusinessException("발행된 매출이 있는 프로젝트는 삭제할 수 없습니다. 프로젝트를 취소 처리하세요.");
        }
        if (assignmentRepository.existsByProjectIdAndPeriodStartDateLessThanEqual(id, LocalDate.now())) {
            throw new BusinessException("이미 시작된 투입이 있는 프로젝트는 삭제할 수 없습니다. 프로젝트를 취소 처리하세요.");
        }
        if (expenseRepository.existsByProjectId(id)) {
            throw new BusinessException("직접비가 등록된 프로젝트는 삭제할 수 없습니다. 프로젝트를 취소 처리하세요.");
        }
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
