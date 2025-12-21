package kr.co.abacus.abms.application.project;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

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

    @Override
    public Project find(Long id) {
        return projectRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ProjectNotFoundException("존재하지 않는 프로젝트입니다: " + id));
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

}
