package kr.co.abacus.abms.application.project;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.auth.CurrentActorPermissionSupport;
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

    private static final String PROJECT_WRITE_PERMISSION_CODE = "project.write";

    private final ProjectRepository projectRepository;
    private final ProjectFinder projectFinder;
    private final PartyRepository partyRepository;
    private final DepartmentRepository departmentRepository;
    private final CurrentActorPermissionSupport permissionSupport;

    @Override
    public Long create(ProjectCreateCommand command) {
        return doCreate(command);
    }

    @Override
    public Long create(CurrentActor actor, ProjectCreateCommand command) {
        validateCanManageLeadDepartment(actor, command.leadDepartmentId(), PROJECT_WRITE_PERMISSION_CODE, "프로젝트 생성 권한 범위를 벗어났습니다.");
        return doCreate(command);
    }

    private Long doCreate(ProjectCreateCommand command) {
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
        return doUpdate(id, command);
    }

    @Override
    public Long update(CurrentActor actor, Long id, ProjectUpdateCommand command) {
        Project project = projectFinder.find(id);
        validateCanManageProject(actor, project, "프로젝트 변경 권한 범위를 벗어났습니다.");
        validateCanManageLeadDepartment(actor, command.leadDepartmentId(), PROJECT_WRITE_PERMISSION_CODE, "프로젝트 변경 부서 권한 범위를 벗어났습니다.");
        return doUpdate(project, command);
    }

    private Long doUpdate(Long id, ProjectUpdateCommand command) {
        Project project = projectFinder.find(id);
        return doUpdate(project, command);
    }

    private Long doUpdate(Project project, ProjectUpdateCommand command) {
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
        doComplete(id);
    }

    @Override
    public void complete(CurrentActor actor, Long id) {
        validateCanManageProject(actor, projectFinder.find(id), "프로젝트 변경 권한 범위를 벗어났습니다.");
        doComplete(id);
    }

    private void doComplete(Long id) {
        Project project = projectFinder.find(id);
        project.complete();

        projectRepository.save(project);
    }

    @Override
    public void cancel(Long id) {
        doCancel(id);
    }

    @Override
    public void cancel(CurrentActor actor, Long id) {
        validateCanManageProject(actor, projectFinder.find(id), "프로젝트 변경 권한 범위를 벗어났습니다.");
        doCancel(id);
    }

    private void doCancel(Long id) {
        Project project = projectFinder.find(id);
        project.cancel();

        projectRepository.save(project);
    }

    @Override
    public void delete(Long id) {
        doDelete(id);
    }

    @Override
    public void delete(CurrentActor actor, Long id) {
        validateCanManageProject(actor, projectFinder.find(id), "프로젝트 변경 권한 범위를 벗어났습니다.");
        doDelete(id);
    }

    private void doDelete(Long id) {
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

    private void validateCanManageProject(CurrentActor actor, Project project, String message) {
        validateCanManageLeadDepartment(actor, project.getLeadDepartmentId(), PROJECT_WRITE_PERMISSION_CODE, message);
    }

    private void validateCanManageLeadDepartment(
            CurrentActor actor,
            Long leadDepartmentId,
            String permissionCode,
            String message
    ) {
        permissionSupport.validateDepartmentAccess(actor, permissionCode, leadDepartmentId, message);
    }

}
