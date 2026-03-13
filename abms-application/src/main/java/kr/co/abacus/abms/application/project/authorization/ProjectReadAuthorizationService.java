package kr.co.abacus.abms.application.project.authorization;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.project.dto.ProjectSearchCondition;
import kr.co.abacus.abms.application.permission.dto.GrantedPermissionDetail;
import kr.co.abacus.abms.application.permission.inbound.PermissionFinder;
import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProjectReadAuthorizationService {

    public static final String PROJECT_READ_PERMISSION_CODE = "project.read";
    public static final String PROJECT_EXCEL_DOWNLOAD_PERMISSION_CODE = "project.excel.download";

    private final PermissionFinder permissionFinder;
    private final ProjectAuthorizationSupport projectAuthorizationSupport;

    public ProjectReadScope resolveScope(Long accountId) {
        return resolveScope(accountId, PROJECT_READ_PERMISSION_CODE);
    }

    public ProjectReadScope resolveExcelDownloadScope(Long accountId) {
        return resolveScope(accountId, PROJECT_EXCEL_DOWNLOAD_PERMISSION_CODE);
    }

    public ProjectSearchCondition authorizeSearchCondition(Long accountId, ProjectSearchCondition condition) {
        return resolveScope(accountId).apply(condition);
    }

    public ProjectSearchCondition authorizeExcelDownloadCondition(Long accountId, ProjectSearchCondition condition) {
        return resolveExcelDownloadScope(accountId).apply(condition);
    }

    public void assertCanRead(Long accountId, Long projectId, Long leadDepartmentId) {
        if (!resolveScope(accountId).canRead(projectId, leadDepartmentId)) {
            throw new AccessDeniedException("프로젝트 조회 권한 범위를 벗어났습니다.");
        }
    }

    public void assertCanDownload(Long accountId, Long projectId, Long leadDepartmentId) {
        if (!resolveExcelDownloadScope(accountId).canRead(projectId, leadDepartmentId)) {
            throw new AccessDeniedException("프로젝트 엑셀 다운로드 권한 범위를 벗어났습니다.");
        }
    }

    private ProjectReadScope resolveScope(Long accountId, String permissionCode) {
        GrantedPermissionDetail permission = permissionFinder.findPermissions(accountId).permissions().stream()
                .filter(grantedPermission -> permissionCode.equals(grantedPermission.code()))
                .findFirst()
                .orElse(null);
        if (permission == null) {
            return ProjectReadScope.none();
        }

        Set<PermissionScope> scopes = permission.scopes();
        if (scopes.contains(PermissionScope.ALL)) {
            return ProjectReadScope.all();
        }

        ProjectAuthorizationContext context = projectAuthorizationSupport.resolveContext(accountId);
        if (context == null) {
            return ProjectReadScope.none();
        }

        Set<Long> allowedLeadDepartmentIds = new LinkedHashSet<>();
        if (scopes.contains(PermissionScope.OWN_DEPARTMENT)) {
            allowedLeadDepartmentIds.add(context.departmentId());
        }
        if (scopes.contains(PermissionScope.OWN_DEPARTMENT_TREE)) {
            allowedLeadDepartmentIds.addAll(projectAuthorizationSupport.resolveDepartmentTree(context.departmentId()));
        }

        Set<Long> allowedProjectIds = new LinkedHashSet<>();
        if (scopes.contains(PermissionScope.CURRENT_PARTICIPATION)) {
            allowedProjectIds.addAll(projectAuthorizationSupport.resolveCurrentParticipationProjectIds(context.employeeId()));
        }

        return new ProjectReadScope(false, allowedProjectIds, allowedLeadDepartmentIds);
    }
}
