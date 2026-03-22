package kr.co.abacus.abms.application.project;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.project.inbound.ProjectRevenuePlanManager;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRevenuePlanRepository;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanDuplicateException;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanCreateRequest;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanNotFoundException;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanUpdateRequest;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class ProjectRevenuePlanModifyService implements ProjectRevenuePlanManager {

    private static final String PROJECT_WRITE_PERMISSION_CODE = "project.write";

    private final DepartmentRepository departmentRepository;
    private final ProjectRepository projectRepository;
    private final ProjectRevenuePlanRepository projectRevenuePlanRepository;
    private final ProjectAuthorizationValidator projectAuthorizationValidator;

    @Override
    public ProjectRevenuePlan create(ProjectRevenuePlanCreateRequest createRequest) {
        return doCreate(createRequest);
    }

    @Override
    public ProjectRevenuePlan create(CurrentActor actor, ProjectRevenuePlanCreateRequest createRequest) {
        validateCanManage(actor, createRequest.projectId());
        return doCreate(createRequest);
    }

    private ProjectRevenuePlan doCreate(ProjectRevenuePlanCreateRequest createRequest) {
        if (projectRevenuePlanRepository.existsByProjectIdAndSequence(createRequest.projectId(), createRequest.sequence())) {
            throw new ProjectRevenuePlanDuplicateException("이미 존재하는 프로젝트 매출 계획입니다.");
        }

        ProjectRevenuePlan projectRevenuePlan = ProjectRevenuePlan.create(createRequest);

        return projectRevenuePlanRepository.save(projectRevenuePlan);
    }

    @Override
    public ProjectRevenuePlan update(Long projectId, Integer sequence, ProjectRevenuePlanUpdateRequest updateRequest) {
        return doUpdate(projectId, sequence, updateRequest);
    }

    @Override
    public ProjectRevenuePlan update(
            CurrentActor actor,
            Long projectId,
            Integer sequence,
            ProjectRevenuePlanUpdateRequest updateRequest
    ) {
        validateCanManage(actor, projectId);
        return doUpdate(projectId, sequence, updateRequest);
    }

    private ProjectRevenuePlan doUpdate(Long projectId, Integer sequence, ProjectRevenuePlanUpdateRequest updateRequest) {
        ProjectRevenuePlan projectRevenuePlan = projectRevenuePlanRepository.findByProjectIdAndSequence(projectId, sequence)
                .orElseThrow(() -> new ProjectRevenuePlanNotFoundException("존재하지 않는 프로젝트 매출 계획입니다."));

        if (!sequence.equals(updateRequest.sequence())
                && projectRevenuePlanRepository.existsByProjectIdAndSequence(projectId, updateRequest.sequence())) {
            throw new ProjectRevenuePlanDuplicateException("이미 존재하는 프로젝트 매출 계획입니다.");
        }

        projectRevenuePlan.update(updateRequest);
        return projectRevenuePlanRepository.save(projectRevenuePlan);
    }

    @Override
    public ProjectRevenuePlan issue(Long projectId, Integer sequence) {
        return doIssue(projectId, sequence);
    }

    @Override
    public ProjectRevenuePlan issue(CurrentActor actor, Long projectId, Integer sequence) {
        validateCanManage(actor, projectId);
        return doIssue(projectId, sequence);
    }

    private ProjectRevenuePlan doIssue(Long projectId, Integer sequence) {
        ProjectRevenuePlan projectRevenuePlan = load(projectId, sequence);
        projectRevenuePlan.issue();
        return projectRevenuePlanRepository.save(projectRevenuePlan);
    }

    @Override
    public ProjectRevenuePlan cancel(Long projectId, Integer sequence) {
        return doCancel(projectId, sequence);
    }

    @Override
    public ProjectRevenuePlan cancel(CurrentActor actor, Long projectId, Integer sequence) {
        validateCanManage(actor, projectId);
        return doCancel(projectId, sequence);
    }

    private ProjectRevenuePlan doCancel(Long projectId, Integer sequence) {
        ProjectRevenuePlan projectRevenuePlan = load(projectId, sequence);
        projectRevenuePlan.cancel();
        return projectRevenuePlanRepository.save(projectRevenuePlan);
    }

    private ProjectRevenuePlan load(Long projectId, Integer sequence) {
        return projectRevenuePlanRepository.findByProjectIdAndSequence(projectId, sequence)
                .orElseThrow(() -> new ProjectRevenuePlanNotFoundException("존재하지 않는 프로젝트 매출 계획입니다."));
    }

    private void validateCanManage(CurrentActor actor, Long projectId) {
        kr.co.abacus.abms.domain.project.Project project = projectRepository.findByIdAndDeletedFalse(projectId)
                .orElseThrow(() -> new kr.co.abacus.abms.domain.project.ProjectNotFoundException("존재하지 않는 프로젝트입니다: " + projectId));
        projectAuthorizationValidator.validateManageProject(actor, project, "프로젝트 변경 권한 범위를 벗어났습니다.");
    }

}
