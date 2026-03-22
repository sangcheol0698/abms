package kr.co.abacus.abms.application.employee;

import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.auth.CurrentActorPermissionSupport;
import kr.co.abacus.abms.application.employee.dto.EmployeeUpdateCommand;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;

@RequiredArgsConstructor
@Component
class EmployeeAuthorizationValidator {

    private static final String EMPLOYEE_WRITE_PERMISSION_CODE = "employee.write";

    private final CurrentActorPermissionSupport permissionSupport;

    void validateCreate(CurrentActor actor, Long departmentId, String message) {
        validateCanManageDepartment(actor, departmentId, message);
    }

    void validateUpdate(CurrentActor actor, Employee target, EmployeeUpdateCommand command) {
        Set<PermissionScope> scopes = permissionSupport.requirePermission(
                actor,
                EMPLOYEE_WRITE_PERMISSION_CODE,
                "직원 수정 권한 범위를 벗어났습니다."
        );
        if (scopes.contains(PermissionScope.ALL)) {
            return;
        }
        if (canSelfUpdate(actor, target, scopes)) {
            validateSelfProfileUpdate(target, command);
            return;
        }

        validateDepartmentScopedUpdate(actor, target, command, scopes);
    }

    void validateManage(CurrentActor actor, Employee target, String message) {
        validateCanManageDepartment(actor, target.getDepartmentId(), message);
    }

    private boolean canSelfUpdate(CurrentActor actor, Employee target, Set<PermissionScope> scopes) {
        return scopes.contains(PermissionScope.SELF)
                && actor.employeeId() != null
                && actor.employeeId().equals(target.getIdOrThrow());
    }

    private void validateSelfProfileUpdate(Employee target, EmployeeUpdateCommand command) {
        if (!isAllowedSelfProfileUpdate(target, command)) {
            throw new AccessDeniedException("본인 프로필은 이름, 생년월일, 아바타만 수정할 수 있습니다.");
        }
    }

    private void validateDepartmentScopedUpdate(
            CurrentActor actor,
            Employee target,
            EmployeeUpdateCommand command,
            Set<PermissionScope> scopes
    ) {
        Set<Long> allowedDepartmentIds = permissionSupport.resolveAllowedDepartmentIds(actor, scopes);
        if (!allowedDepartmentIds.contains(target.getDepartmentId())) {
            throw new AccessDeniedException("직원 수정 권한 범위를 벗어났습니다.");
        }
        if (!allowedDepartmentIds.contains(command.departmentId())) {
            throw new AccessDeniedException("직원 배치 권한 범위를 벗어났습니다.");
        }
    }

    private void validateCanManageDepartment(CurrentActor actor, Long departmentId, String message) {
        permissionSupport.validateDepartmentAccess(actor, EMPLOYEE_WRITE_PERMISSION_CODE, departmentId, message);
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
