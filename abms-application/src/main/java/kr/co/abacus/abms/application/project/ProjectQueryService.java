package kr.co.abacus.abms.application.project;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.application.department.DepartmentQueryService;
import kr.co.abacus.abms.application.party.PartyQueryService;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.project.dto.ProjectDetail;
import kr.co.abacus.abms.application.project.dto.ProjectSearchCondition;
import kr.co.abacus.abms.application.project.dto.ProjectSummary;
import kr.co.abacus.abms.application.project.inbound.ProjectFinder;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectNotFoundException;
import kr.co.abacus.abms.domain.project.ProjectStatus;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProjectQueryService implements ProjectFinder {

    private final ProjectRepository projectRepository;
    private final PartyQueryService partyQueryService;
    private final DepartmentQueryService departmentQueryService;

    @Override
    public Project find(Long id) {
        return projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ProjectNotFoundException("존재하지 않는 프로젝트입니다: " + id));
    }

    public ProjectDetail findDetail(Long id) {
        Project project = projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ProjectNotFoundException("존재하지 않는 프로젝트입니다: " + id));

        String partyName = partyQueryService.getPartyName(project.getPartyId());
        String leadDepartmentName = departmentQueryService.find(project.getLeadDepartmentId()).getName();

        return ProjectDetail.of(project, partyName, leadDepartmentName);
    }

    @Override
    public List<Project> findAll() {
        return projectRepository.findAllByDeletedFalse();
    }

    @Override
    public Page<Project> findAll(Pageable pageable) {
        return projectRepository.findAllByDeletedFalse(pageable);
    }

    @Override
    public List<Project> findAllByPartyId(Long partyId) {
        return projectRepository.findAllByPartyIdAndDeletedFalse(partyId);
    }

    @Override
    public List<Project> findAllByStatus(ProjectStatus status) {
        return projectRepository.findAllByStatusAndDeletedFalse(status);
    }

    @Override
    public Page<ProjectSummary> search(ProjectSearchCondition condition, Pageable pageable) {
        return projectRepository.search(condition, pageable);
    }

}
