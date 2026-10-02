package kr.co.abacus.abms.project;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.common.domain.NotFoundException;
import kr.co.abacus.abms.project.ProjectRevenuePlan.RevenuePlanInfo;
import kr.co.abacus.abms.security.LoginUser;

/**
 * 프로젝트 매출(청구) 계획 관리.
 */
@Service
@Transactional
public class ProjectRevenueService {

    private final ProjectService projectService;
    private final ProjectRevenuePlanRepository revenuePlanRepository;

    public ProjectRevenueService(ProjectService projectService, ProjectRevenuePlanRepository revenuePlanRepository) {
        this.projectService = projectService;
        this.revenuePlanRepository = revenuePlanRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectRevenuePlan> plans(Long projectId) {
        return revenuePlanRepository.findAllByProjectIdOrderBySequenceAsc(projectId);
    }

    @Transactional(readOnly = true)
    public int nextSequence(Long projectId) {
        return revenuePlanRepository.findMaxSequence(projectId) + 1;
    }

    @Transactional(readOnly = true)
    public ProjectRevenuePlan get(Long projectId, Long planId) {
        return revenuePlanRepository.findById(planId)
                .filter(plan -> plan.getProjectId().equals(projectId))
                .orElseThrow(() -> NotFoundException.of("매출 계획", planId));
    }

    public ProjectRevenuePlan add(LoginUser user, Long projectId, RevenuePlanInfo info) {
        Project project = projectService.getForWrite(user, projectId);
        if (revenuePlanRepository.existsByProjectIdAndSequence(projectId, info.sequence())) {
            throw new BusinessException(info.sequence() + "차 매출 계획이 이미 있습니다.");
        }
        checkTotal(project, plans(projectId), info.amount());
        return revenuePlanRepository.save(ProjectRevenuePlan.create(projectId, info));
    }

    public void update(LoginUser user, Long projectId, Long planId, RevenuePlanInfo info) {
        Project project = projectService.getForWrite(user, projectId);
        ProjectRevenuePlan plan = get(projectId, planId);
        if (revenuePlanRepository.existsByProjectIdAndSequenceAndIdNot(projectId, info.sequence(), planId)) {
            throw new BusinessException(info.sequence() + "차 매출 계획이 이미 있습니다.");
        }
        checkTotal(project, plans(projectId).stream().filter(p -> !p.id().equals(planId)).toList(), info.amount());
        plan.update(info);
    }

    public void issue(LoginUser user, Long projectId, Long planId) {
        projectService.getForWrite(user, projectId);
        get(projectId, planId).issue();
    }

    public void cancelIssue(LoginUser user, Long projectId, Long planId) {
        projectService.getForWrite(user, projectId);
        get(projectId, planId).cancelIssue();
    }

    public void delete(LoginUser user, Long projectId, Long planId) {
        projectService.getForWrite(user, projectId);
        ProjectRevenuePlan plan = get(projectId, planId);
        if (plan.isIssued()) {
            throw new BusinessException("발행된 매출은 삭제할 수 없습니다. 발행을 먼저 취소하세요.");
        }
        plan.softDelete(user.accountId());
    }

    /** 매출 계획 합계는 계약금액을 넘을 수 없다. */
    private static void checkTotal(Project project, List<ProjectRevenuePlan> others, Money amount) {
        Money total = others.stream().map(ProjectRevenuePlan::getAmount).reduce(Money.ZERO, Money::plus).plus(amount);
        if (total.compareTo(project.getContractAmount()) > 0) {
            throw new BusinessException("매출 계획 합계(" + total.formatted() + "원)가 계약금액("
                    + project.getContractAmount().formatted() + "원)을 초과합니다.");
        }
    }

}
