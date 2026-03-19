package kr.co.abacus.abms.application.project;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.application.project.inbound.ProjectRevenuePlanManager;
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

    private final ProjectRevenuePlanRepository projectRevenuePlanRepository;

    @Override
    public ProjectRevenuePlan create(ProjectRevenuePlanCreateRequest createRequest) {
        if (projectRevenuePlanRepository.existsByProjectIdAndSequence(createRequest.projectId(), createRequest.sequence())) {
            throw new ProjectRevenuePlanDuplicateException("이미 존재하는 프로젝트 매출 계획입니다.");
        }

        ProjectRevenuePlan projectRevenuePlan = ProjectRevenuePlan.create(createRequest);

        return projectRevenuePlanRepository.save(projectRevenuePlan);
    }

    @Override
    public ProjectRevenuePlan update(Long projectId, Integer sequence, ProjectRevenuePlanUpdateRequest updateRequest) {
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
        ProjectRevenuePlan projectRevenuePlan = load(projectId, sequence);
        projectRevenuePlan.issue();
        return projectRevenuePlanRepository.save(projectRevenuePlan);
    }

    @Override
    public ProjectRevenuePlan cancel(Long projectId, Integer sequence) {
        ProjectRevenuePlan projectRevenuePlan = load(projectId, sequence);
        projectRevenuePlan.cancel();
        return projectRevenuePlanRepository.save(projectRevenuePlan);
    }

    private ProjectRevenuePlan load(Long projectId, Integer sequence) {
        return projectRevenuePlanRepository.findByProjectIdAndSequence(projectId, sequence)
                .orElseThrow(() -> new ProjectRevenuePlanNotFoundException("존재하지 않는 프로젝트 매출 계획입니다."));
    }

}
