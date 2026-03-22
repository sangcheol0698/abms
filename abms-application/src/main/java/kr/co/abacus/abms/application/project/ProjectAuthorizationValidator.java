package kr.co.abacus.abms.application.project;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.auth.CurrentActorPermissionSupport;
import kr.co.abacus.abms.application.project.dto.ProjectCreateCommand;
import kr.co.abacus.abms.application.project.dto.ProjectUpdateCommand;
import kr.co.abacus.abms.domain.project.Project;

@RequiredArgsConstructor
@Component
class ProjectAuthorizationValidator {

    private static final String PROJECT_WRITE_PERMISSION_CODE = "project.write";

    private final CurrentActorPermissionSupport permissionSupport;

    void validateCreate(CurrentActor actor, ProjectCreateCommand command) {
        permissionSupport.validateDepartmentAccess(
                actor,
                PROJECT_WRITE_PERMISSION_CODE,
                command.leadDepartmentId(),
                "프로젝트 생성 권한 범위를 벗어났습니다."
        );
    }

    void validateUpdate(CurrentActor actor, Project project, ProjectUpdateCommand command) {
        validateManage(actor, project, "프로젝트 변경 권한 범위를 벗어났습니다.");
        permissionSupport.validateDepartmentAccess(
                actor,
                PROJECT_WRITE_PERMISSION_CODE,
                command.leadDepartmentId(),
                "프로젝트 변경 부서 권한 범위를 벗어났습니다."
        );
    }

    void validateManage(CurrentActor actor, Project project, String message) {
        permissionSupport.validateDepartmentAccess(
                actor,
                PROJECT_WRITE_PERMISSION_CODE,
                project.getLeadDepartmentId(),
                message
        );
    }
}
