package kr.co.abacus.abms.application.project;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.project.inbound.ProjectFinder;
import kr.co.abacus.abms.application.project.inbound.ProjectManager;
import kr.co.abacus.abms.application.project.dto.ProjectCreateCommand;
import kr.co.abacus.abms.application.project.dto.ProjectUpdateCommand;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.domain.party.PartyNotFoundException;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectCodeDuplicateException;

@RequiredArgsConstructor
@Transactional
@Service
public class ProjectModifyService implements ProjectManager {

    private final ProjectRepository projectRepository;
    private final ProjectFinder projectFinder;
    private final PartyRepository partyRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public Long create(ProjectCreateCommand command) {
        if (projectRepository.existsByCode(command.code())) {
            throw new ProjectCodeDuplicateException("이미 존재하는 프로젝트 코드입니다: " + command.code());
        }
        validateActivePartyExists(command.partyId());
        validateLeadDepartmentExists(command.leadDepartmentId());

        Project project = Project.create(
                command.partyId(),
                command.leadDepartmentId(),
                command.code(),
                command.name(),
                command.description(),
                command.status(),
                command.contractAmount(),
                command.startDate(),
                command.endDate());

        return projectRepository.save(project).getIdOrThrow();
    }

    @Override
    public Long update(Long id, ProjectUpdateCommand command) {
        Project project = projectFinder.find(id);
        validateActivePartyExists(command.partyId());
        validateLeadDepartmentExists(command.leadDepartmentId());
        project.update(
                command.partyId(),
                command.leadDepartmentId(),
                command.name(),
                command.description(),
                command.status(),
                command.contractAmount(),
                command.startDate(),
                command.endDate());

        return projectRepository.save(project).getIdOrThrow();
    }

    @Override
    public void complete(Long id) {
        Project project = projectFinder.find(id);
        project.complete();

        projectRepository.save(project);
    }

    @Override
    public void cancel(Long id) {
        Project project = projectFinder.find(id);
        project.cancel();

        projectRepository.save(project);
    }

    @Override
    public void delete(Long id) {
        Project project = projectFinder.find(id);
        project.softDelete(null);

        projectRepository.save(project);
    }

    private void validateActivePartyExists(Long partyId) {
        if (partyRepository.findByIdAndDeletedFalse(partyId).isEmpty()) {
            throw new PartyNotFoundException("존재하지 않거나 비활성화된 협력사입니다: " + partyId);
        }
    }

    private void validateLeadDepartmentExists(Long leadDepartmentId) {
        if (leadDepartmentId == null || !departmentRepository.existsByIdAndDeletedFalse(leadDepartmentId)) {
            throw new IllegalArgumentException("주관 부서를 선택하세요.");
        }
    }

}
