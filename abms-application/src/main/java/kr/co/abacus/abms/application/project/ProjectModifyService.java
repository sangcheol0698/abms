package kr.co.abacus.abms.application.project;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.project.dto.ProjectCreateCommand;
import kr.co.abacus.abms.application.project.dto.ProjectUpdateCommand;
import kr.co.abacus.abms.application.project.inbound.ProjectFinder;
import kr.co.abacus.abms.application.project.inbound.ProjectManager;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
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
    private final ProjectAuthorizationValidator projectAuthorizationValidator;

    @Override
    public Long create(CurrentActor actor, ProjectCreateCommand command) {
        projectAuthorizationValidator.validateCreate(actor, command);

        validateDuplicateProjectCode(command);
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

    private void validateDuplicateProjectCode(ProjectCreateCommand command) {
        if (projectRepository.existsByCode(command.code())) {
            throw new ProjectCodeDuplicateException("이미 존재하는 프로젝트 코드입니다: " + command.code());
        }
    }

    @Override
    public Long update(CurrentActor actor, Long id, ProjectUpdateCommand command) {
        Project project = projectFinder.find(id);
        projectAuthorizationValidator.validateUpdate(actor, project, command);
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
    public void complete(CurrentActor actor, Long id) {
        Project project = projectFinder.find(id);
        projectAuthorizationValidator.validateManage(actor, project, "프로젝트 변경 권한 범위를 벗어났습니다.");
        project.complete();

        projectRepository.save(project);
    }

    @Override
    public void cancel(CurrentActor actor, Long id) {
        Project project = projectFinder.find(id);
        projectAuthorizationValidator.validateManage(actor, project, "프로젝트 변경 권한 범위를 벗어났습니다.");
        project.cancel();

        projectRepository.save(project);
    }

    @Override
    public void delete(CurrentActor actor, Long id) {
        Project project = projectFinder.find(id);
        projectAuthorizationValidator.validateManage(actor, project, "프로젝트 변경 권한 범위를 벗어났습니다.");
        project.softDelete(null);

        projectRepository.save(project);
    }

    private void validateActivePartyExists(Long partyId) {
        if (partyRepository.findByIdAndDeletedFalse(partyId).isEmpty()) {
            throw new PartyNotFoundException("존재하지 않거나 비활성화된 협력사입니다: " + partyId);
        }
    }

    private void validateLeadDepartmentExists(Long leadDepartmentId) {
        if (!departmentRepository.existsByIdAndDeletedFalse(leadDepartmentId)) {
            throw new IllegalArgumentException("주관 부서를 선택하세요.");
        }
    }

}
