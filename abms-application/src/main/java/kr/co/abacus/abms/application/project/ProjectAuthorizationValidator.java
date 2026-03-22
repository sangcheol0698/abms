package kr.co.abacus.abms.application.project;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.auth.CurrentActorPermissionSupport;
import kr.co.abacus.abms.application.project.dto.ProjectCreateCommand;
import kr.co.abacus.abms.application.project.dto.ProjectUpdateCommand;
import kr.co.abacus.abms.domain.project.Project;

@RequiredArgsConstructor
@Component
public class ProjectAuthorizationValidator {

    private static final String PROJECT_WRITE_PERMISSION_CODE = "project.write";
    private static final String PROJECT_EXCEL_UPLOAD_PERMISSION_CODE = "project.excel.upload";

    private final CurrentActorPermissionSupport permissionSupport;

    public void validateCreate(CurrentActor actor, ProjectCreateCommand command) {
        permissionSupport.validateDepartmentAccess(
                actor,
                PROJECT_WRITE_PERMISSION_CODE,
                command.leadDepartmentId(),
                "프로젝트 생성 권한 범위를 벗어났습니다."
        );
    }

    public void validateUpdate(CurrentActor actor, Project project, ProjectUpdateCommand command) {
        validateManageProject(actor, project, "프로젝트 변경 권한 범위를 벗어났습니다.");
        permissionSupport.validateDepartmentAccess(
                actor,
                PROJECT_WRITE_PERMISSION_CODE,
                command.leadDepartmentId(),
                "프로젝트 변경 부서 권한 범위를 벗어났습니다."
        );
    }

    public void validateManageProject(CurrentActor actor, Project project, String message) {
        permissionSupport.validateDepartmentAccess(
                actor,
                PROJECT_WRITE_PERMISSION_CODE,
                project.getLeadDepartmentId(),
                message
        );
    }

    public CurrentActor authorizeExcelUpload(CurrentActor actor, List<ProjectCreateCommand> commands) {
        java.util.Set<kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope> scopes = permissionSupport.requirePermission(
                actor,
                PROJECT_EXCEL_UPLOAD_PERMISSION_CODE,
                "프로젝트 엑셀 업로드 권한 범위를 벗어났습니다."
        );
        if (!scopes.contains(kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope.ALL)) {
            java.util.Set<Long> allowedDepartmentIds = permissionSupport.resolveAllowedDepartmentIds(actor, scopes);
            boolean unauthorizedExists = commands.stream()
                    .map(ProjectCreateCommand::leadDepartmentId)
                    .anyMatch(leadDepartmentId -> !allowedDepartmentIds.contains(leadDepartmentId));
            if (unauthorizedExists) {
                throw new AccessDeniedException("프로젝트 엑셀 업로드 권한 범위를 벗어났습니다.");
            }
        }

        return new CurrentActor(
                actor.accountId(),
                actor.username(),
                actor.employeeId(),
                actor.departmentId(),
                Map.of(PROJECT_WRITE_PERMISSION_CODE, scopes)
        );
    }
}
