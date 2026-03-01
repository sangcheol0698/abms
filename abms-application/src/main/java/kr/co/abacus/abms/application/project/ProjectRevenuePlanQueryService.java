package kr.co.abacus.abms.application.project;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.application.department.DepartmentQueryService;
import kr.co.abacus.abms.application.party.PartyQueryService;
import kr.co.abacus.abms.application.project.dto.ProjectSearchCondition;
import kr.co.abacus.abms.application.project.dto.ProjectSummary;
import kr.co.abacus.abms.application.project.inbound.ProjectFinder;
import kr.co.abacus.abms.application.project.inbound.ProjectRevenuePlanFinder;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRevenuePlanRepository;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectNotFoundException;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanDuplicateException;
import kr.co.abacus.abms.domain.project.ProjectStatus;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProjectRevenuePlanQueryService implements ProjectRevenuePlanFinder {

    private final ProjectRevenuePlanRepository projectRevenuePlanRepository;

    @Override
    public ProjectRevenuePlan findByProjectIdAndSequence(Long projectId, Integer sequence) {
        return projectRevenuePlanRepository.findByProjectIdAndSequence(projectId, sequence)
                .orElseThrow(() -> new ProjectRevenuePlanDuplicateException("존재하지 않는 프로젝트 매출 계획입니다."));
    }

    @Override
    public List<ProjectRevenuePlan> findByProjectId(Long projectId) {
        return projectRevenuePlanRepository.findByProjectId(projectId);
    }

}
