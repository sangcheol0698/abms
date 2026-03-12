package kr.co.abacus.abms.application.employee.authorization;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.permission.dto.GrantedPermissionDetail;
import kr.co.abacus.abms.application.permission.inbound.PermissionFinder;
import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class EmployeeReadAuthorizationService {

    public static final String EMPLOYEE_READ_PERMISSION_CODE = "employee.read";
    public static final String EMPLOYEE_EXCEL_DOWNLOAD_PERMISSION_CODE = "employee.excel.download";

    private final PermissionFinder permissionFinder;
    private final EmployeeAuthorizationSupport employeeAuthorizationSupport;

    public EmployeeReadScope resolveScope(Long accountId) {
        return resolveScope(accountId, EMPLOYEE_READ_PERMISSION_CODE);
    }

    public EmployeeReadScope resolveExcelDownloadScope(Long accountId) {
        return resolveScope(accountId, EMPLOYEE_EXCEL_DOWNLOAD_PERMISSION_CODE);
    }

    private EmployeeReadScope resolveScope(Long accountId, String permissionCode) {
        GrantedPermissionDetail permission = permissionFinder.findPermissions(accountId).permissions().stream()
                .filter(grantedPermission -> permissionCode.equals(grantedPermission.code()))
                .findFirst()
                .orElse(null);
        if (permission == null) {
            return EmployeeReadScope.none();
        }

        Set<PermissionScope> scopes = permission.scopes();
        if (scopes.contains(PermissionScope.ALL)) {
            return EmployeeReadScope.all();
        }

        EmployeeAuthorizationContext context = employeeAuthorizationSupport.resolveContext(accountId);
        if (context == null) {
            return EmployeeReadScope.none();
        }

        Long selfDepartmentId = context.departmentId();
        Set<Long> allowedDepartmentIds = new LinkedHashSet<>();
        if (scopes.contains(PermissionScope.OWN_DEPARTMENT)) {
            allowedDepartmentIds.add(selfDepartmentId);
        }
        if (scopes.contains(PermissionScope.OWN_DEPARTMENT_TREE)) {
            allowedDepartmentIds.addAll(employeeAuthorizationSupport.resolveDepartmentTree(selfDepartmentId));
        }

        Long selfEmployeeId = scopes.contains(PermissionScope.SELF) ? context.employeeId() : null;

        return new EmployeeReadScope(
                false,
                allowedDepartmentIds,
                selfEmployeeId,
                selfEmployeeId != null ? selfDepartmentId : null
        );
    }

}
