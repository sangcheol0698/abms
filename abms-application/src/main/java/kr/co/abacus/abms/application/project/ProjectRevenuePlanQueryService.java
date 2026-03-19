package kr.co.abacus.abms.application.project;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.application.project.inbound.ProjectRevenuePlanFinder;
import kr.co.abacus.abms.application.project.outbound.ProjectRevenuePlanRepository;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanNotFoundException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProjectRevenuePlanQueryService implements ProjectRevenuePlanFinder {

    private final ProjectRevenuePlanRepository projectRevenuePlanRepository;

    @Override
    public ProjectRevenuePlan findByProjectIdAndSequence(Long projectId, Integer sequence) {
        return projectRevenuePlanRepository.findByProjectIdAndSequence(projectId, sequence)
                .orElseThrow(() -> new ProjectRevenuePlanNotFoundException("존재하지 않는 프로젝트 매출 계획입니다."));
    }

    @Override
    public List<ProjectRevenuePlan> findByProjectId(Long projectId) {
        return projectRevenuePlanRepository.findByProjectId(projectId);
    }

}
