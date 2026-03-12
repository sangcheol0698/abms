package kr.co.abacus.abms.application.employee.authorization;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.employee.dto.EmployeeCreateCommand;
import kr.co.abacus.abms.application.employee.dto.EmployeeUpdateCommand;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.permission.dto.GrantedPermissionDetail;
import kr.co.abacus.abms.application.permission.inbound.PermissionFinder;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeNotFoundException;
import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class EmployeeWriteAuthorizationService {

    public static final String EMPLOYEE_WRITE_PERMISSION_CODE = "employee.write";
    public static final String EMPLOYEE_EXCEL_UPLOAD_PERMISSION_CODE = "employee.excel.upload";

    private final PermissionFinder permissionFinder;
    private final EmployeeAuthorizationSupport employeeAuthorizationSupport;
    private final EmployeeRepository employeeRepository;

    public EmployeeWriteScope resolveScope(Long accountId) {
        return resolveScope(accountId, EMPLOYEE_WRITE_PERMISSION_CODE);
    }

    public EmployeeWriteScope resolveExcelUploadScope(Long accountId) {
        return resolveScope(accountId, EMPLOYEE_EXCEL_UPLOAD_PERMISSION_CODE);
    }

    private EmployeeWriteScope resolveScope(Long accountId, String permissionCode) {
        GrantedPermissionDetail permission = permissionFinder.findPermissions(accountId).permissions().stream()
                .filter(grantedPermission -> permissionCode.equals(grantedPermission.code()))
                .findFirst()
                .orElse(null);
        if (permission == null) {
            return EmployeeWriteScope.none();
        }

        Set<PermissionScope> scopes = permission.scopes();
        if (scopes.contains(PermissionScope.ALL)) {
            return EmployeeWriteScope.all();
        }

        EmployeeAuthorizationContext context = employeeAuthorizationSupport.resolveContext(accountId);
        if (context == null) {
            return EmployeeWriteScope.none();
        }

        Set<Long> allowedDepartmentIds = new LinkedHashSet<>();
        if (scopes.contains(PermissionScope.OWN_DEPARTMENT)) {
            allowedDepartmentIds.add(context.departmentId());
        }
        if (scopes.contains(PermissionScope.OWN_DEPARTMENT_TREE)) {
            allowedDepartmentIds.addAll(employeeAuthorizationSupport.resolveDepartmentTree(context.departmentId()));
        }

        Long selfEmployeeId = scopes.contains(PermissionScope.SELF) ? context.employeeId() : null;

        return new EmployeeWriteScope(
                false,
                allowedDepartmentIds,
                selfEmployeeId,
                selfEmployeeId != null ? context.departmentId() : null
        );
    }

    public void assertCanCreate(Long accountId, Long departmentId) {
        if (!resolveScope(accountId).canCreateIn(departmentId)) {
            throw new AccessDeniedException("직원 생성 권한 범위를 벗어났습니다.");
        }
    }

    public void assertCanUpdate(Long accountId, Long employeeId, EmployeeUpdateCommand command) {
        Employee target = employeeRepository.findByIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("존재하지 않는 직원입니다: " + employeeId));
        EmployeeWriteScope scope = resolveScope(accountId);

        if (scope.isSelfOnly()) {
            if (!scope.canUpdateOwnProfile(target.getIdOrThrow())) {
                throw new AccessDeniedException("직원 수정 권한 범위를 벗어났습니다.");
            }
            if (!isAllowedSelfProfileUpdate(target, command)) {
                throw new AccessDeniedException("본인 프로필은 이름, 생년월일, 아바타만 수정할 수 있습니다.");
            }
            return;
        }

        if (!scope.canManageTarget(target.getDepartmentId())) {
            throw new AccessDeniedException("직원 수정 권한 범위를 벗어났습니다.");
        }
        if (!scope.canCreateIn(command.departmentId())) {
            throw new AccessDeniedException("직원 배치 권한 범위를 벗어났습니다.");
        }
    }

    public void assertCanManage(Long accountId, Long employeeId) {
        Employee target = employeeRepository.findByIdAndDeletedFalse(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("존재하지 않는 직원입니다: " + employeeId));

        assertCanManageTarget(resolveScope(accountId), target, "직원 변경 권한 범위를 벗어났습니다.");
    }

    public void assertCanManageIncludingDeleted(Long accountId, Long employeeId) {
        Employee target = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException("존재하지 않는 직원입니다: " + employeeId));

        assertCanManageTarget(resolveScope(accountId), target, "직원 변경 권한 범위를 벗어났습니다.");
    }

    public void assertCanUpload(Long accountId, java.util.List<EmployeeCreateCommand> commands) {
        EmployeeWriteScope scope = resolveExcelUploadScope(accountId);
        boolean unauthorizedExists = commands.stream()
                .map(EmployeeCreateCommand::departmentId)
                .anyMatch(departmentId -> !scope.canCreateIn(departmentId));
        if (unauthorizedExists) {
            throw new AccessDeniedException("직원 엑셀 업로드 권한 범위를 벗어났습니다.");
        }
    }

    private void assertCanManageTarget(EmployeeWriteScope scope, Employee target, String message) {
        if (!scope.canManageTarget(target.getDepartmentId())) {
            throw new AccessDeniedException(message);
        }
    }

    private boolean isAllowedSelfProfileUpdate(Employee target, EmployeeUpdateCommand command) {
        return target.getDepartmentId().equals(command.departmentId())
                && target.getEmail().address().equals(command.email())
                && target.getJoinDate().equals(command.joinDate())
                && target.getPosition() == command.position()
                && target.getType() == command.type()
                && target.getGrade() == command.grade()
                && java.util.Objects.equals(target.getMemo(), command.memo());
    }
}
