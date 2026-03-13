package kr.co.abacus.abms.application.project.authorization;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.permission.dto.GrantedPermissionDetail;
import kr.co.abacus.abms.application.permission.inbound.PermissionFinder;
import kr.co.abacus.abms.application.project.dto.ProjectCreateCommand;
import kr.co.abacus.abms.application.project.dto.ProjectUpdateCommand;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectNotFoundException;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProjectWriteAuthorizationService {

    public static final String PROJECT_WRITE_PERMISSION_CODE = "project.write";
    public static final String PROJECT_EXCEL_UPLOAD_PERMISSION_CODE = "project.excel.upload";

    private final PermissionFinder permissionFinder;
    private final ProjectAuthorizationSupport projectAuthorizationSupport;
    private final ProjectRepository projectRepository;

    public ProjectWriteScope resolveScope(Long accountId) {
        return resolveScope(accountId, PROJECT_WRITE_PERMISSION_CODE);
    }

    public ProjectWriteScope resolveExcelUploadScope(Long accountId) {
        return resolveScope(accountId, PROJECT_EXCEL_UPLOAD_PERMISSION_CODE);
    }

    public void assertCanCreate(Long accountId, Long leadDepartmentId) {
        if (!resolveScope(accountId).canManage(leadDepartmentId)) {
            throw new AccessDeniedException("프로젝트 생성 권한 범위를 벗어났습니다.");
        }
    }

    public void assertCanUpdate(Long accountId, Long projectId, ProjectUpdateCommand command) {
        Project project = projectRepository.findByIdAndDeletedFalse(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("존재하지 않는 프로젝트입니다: " + projectId));

        ProjectWriteScope scope = resolveScope(accountId);
        if (!scope.canManage(project.getLeadDepartmentId())) {
            throw new AccessDeniedException("프로젝트 변경 권한 범위를 벗어났습니다.");
        }
        if (!scope.canManage(command.leadDepartmentId())) {
            throw new AccessDeniedException("프로젝트 변경 부서 권한 범위를 벗어났습니다.");
        }
    }

    public void assertCanManage(Long accountId, Long projectId) {
        Project project = projectRepository.findByIdAndDeletedFalse(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("존재하지 않는 프로젝트입니다: " + projectId));

        if (!resolveScope(accountId).canManage(project.getLeadDepartmentId())) {
            throw new AccessDeniedException("프로젝트 변경 권한 범위를 벗어났습니다.");
        }
    }

    public void assertCanUpload(Long accountId, List<ProjectCreateCommand> commands) {
        ProjectWriteScope scope = resolveExcelUploadScope(accountId);
        boolean unauthorizedExists = commands.stream()
                .map(ProjectCreateCommand::leadDepartmentId)
                .anyMatch(leadDepartmentId -> !scope.canManage(leadDepartmentId));
        if (unauthorizedExists) {
            throw new AccessDeniedException("프로젝트 엑셀 업로드 권한 범위를 벗어났습니다.");
        }
    }

    private ProjectWriteScope resolveScope(Long accountId, String permissionCode) {
        GrantedPermissionDetail permission = permissionFinder.findPermissions(accountId).permissions().stream()
                .filter(grantedPermission -> permissionCode.equals(grantedPermission.code()))
                .findFirst()
                .orElse(null);
        if (permission == null) {
            return ProjectWriteScope.none();
        }

        Set<PermissionScope> scopes = permission.scopes();
        if (scopes.contains(PermissionScope.ALL)) {
            return ProjectWriteScope.all();
        }

        ProjectAuthorizationContext context = projectAuthorizationSupport.resolveContext(accountId);
        if (context == null) {
            return ProjectWriteScope.none();
        }

        Set<Long> allowedLeadDepartmentIds = new LinkedHashSet<>();
        if (scopes.contains(PermissionScope.OWN_DEPARTMENT)) {
            allowedLeadDepartmentIds.add(context.departmentId());
        }
        if (scopes.contains(PermissionScope.OWN_DEPARTMENT_TREE)) {
            allowedLeadDepartmentIds.addAll(projectAuthorizationSupport.resolveDepartmentTree(context.departmentId()));
        }

        return new ProjectWriteScope(false, allowedLeadDepartmentIds);
    }
}
