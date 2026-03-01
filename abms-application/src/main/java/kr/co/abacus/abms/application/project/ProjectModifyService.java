package kr.co.abacus.abms.application.project;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.project.inbound.ProjectFinder;
import kr.co.abacus.abms.application.project.inbound.ProjectManager;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.domain.party.PartyNotFoundException;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectCodeDuplicateException;
import kr.co.abacus.abms.domain.project.ProjectCreateRequest;
import kr.co.abacus.abms.domain.project.ProjectUpdateRequest;

@RequiredArgsConstructor
@Transactional
@Service
public class ProjectModifyService implements ProjectManager {

    private final ProjectRepository projectRepository;
    private final ProjectFinder projectFinder;
    private final PartyRepository partyRepository;

    @Override
    public Project create(ProjectCreateRequest request) {
        if (projectRepository.existsByCode(request.code())) {
            throw new ProjectCodeDuplicateException("이미 존재하는 프로젝트 코드입니다: " + request.code());
        }
        validateActivePartyExists(request.partyId());

        Project project = Project.create(request);

        return projectRepository.save(project);
    }

    @Override
    public Project update(Long id, ProjectUpdateRequest request) {
        Project project = projectFinder.find(id);
        validateActivePartyExists(request.partyId());
        project.update(request);

        return projectRepository.save(project);
    }

    @Override
    public Project complete(Long id) {
        Project project = projectFinder.find(id);
        project.complete();

        return projectRepository.save(project);
    }

    @Override
    public Project cancel(Long id) {
        Project project = projectFinder.find(id);
        project.cancel();

        return projectRepository.save(project);
    }

    @Override
    public void delete(Long id) {
        Project project = projectFinder.find(id);
        project.softDelete("system");

        projectRepository.save(project);
    }

    private void validateActivePartyExists(Long partyId) {
        if (partyRepository.findByIdAndDeletedFalse(partyId).isEmpty()) {
            throw new PartyNotFoundException("존재하지 않거나 비활성화된 협력사입니다: " + partyId);
        }
    }

}
