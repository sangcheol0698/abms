package kr.co.abacus.abms.application.project;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.application.project.inbound.ProjectRevenuePlanManager;
import kr.co.abacus.abms.application.project.outbound.ProjectRevenuePlanRepository;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanDuplicateException;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanCreateRequest;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class ProjectRevenuePlanModifyService implements ProjectRevenuePlanManager {

    private final ProjectRevenuePlanRepository projectRevenuePlanRepository;

    @Override
    public ProjectRevenuePlan create(ProjectRevenuePlanCreateRequest createRequest) {
        if (projectRevenuePlanRepository.existsByProjectIdAndSequence(createRequest.projectId(), createRequest.sequence())) {
            throw new ProjectRevenuePlanDuplicateException("이미 존재하는 프로젝트 수금 계획입니다");
        }

        ProjectRevenuePlan projectRevenuePlan = ProjectRevenuePlan.create(createRequest);

        return projectRevenuePlanRepository.save(projectRevenuePlan);
    }

}
