package kr.co.abacus.abms.application.employee;

import java.time.LocalDate;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.auth.CurrentActorPermissionSupport;
import kr.co.abacus.abms.application.auth.outbound.AccountRepository;
import kr.co.abacus.abms.application.auth.outbound.SessionInvalidator;
import kr.co.abacus.abms.application.department.event.OrganizationChartInvalidationRequestedEvent;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.dto.EmployeeCreateCommand;
import kr.co.abacus.abms.application.employee.dto.EmployeeUpdateCommand;
import kr.co.abacus.abms.application.employee.inbound.EmployeeManager;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.positionhistory.outbound.PositionHistoryRepository;
import kr.co.abacus.abms.domain.department.DepartmentNotFoundException;
import kr.co.abacus.abms.domain.employee.DuplicateEmailException;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeeNotFoundException;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;
import kr.co.abacus.abms.domain.positionhistory.PositionHistory;
import kr.co.abacus.abms.domain.positionhistory.PositionHistoryCreateRequest;
import kr.co.abacus.abms.domain.shared.Email;
import kr.co.abacus.abms.domain.shared.Period;

@RequiredArgsConstructor
@Transactional
@Service
public class EmployeeModifyService implements EmployeeManager {

    private static final String EMPLOYEE_WRITE_PERMISSION_CODE = "employee.write";

    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionHistoryRepository positionHistoryRepository;
    private final SessionInvalidator sessionInvalidator;
    private final ApplicationEventPublisher eventPublisher;
    private final CurrentActorPermissionSupport permissionSupport;

    @Override
    public Long create(EmployeeCreateCommand command) {
        return doCreate(command);
    }

    @Override
    public Long create(CurrentActor actor, EmployeeCreateCommand command) {
        validateCanCreate(actor, command.departmentId(), EMPLOYEE_WRITE_PERMISSION_CODE, "직원 생성 권한 범위를 벗어났습니다.");
        return doCreate(command);
    }

    private Long doCreate(EmployeeCreateCommand command) {
        validateDepartmentExists(command.departmentId());
        validateDuplicateEmail(command.email());

        Employee employee = command.toEntity();

        Long id = employeeRepository.save(employee).getIdOrThrow();

        /**
         * 직급 이력 생성 추가
         * 추후에 도메인 이벤트 방식으로 변경
         */
        PositionHistory positionHistory = PositionHistory.create(new PositionHistoryCreateRequest(
                employee.getIdOrThrow(),
                new Period(employee.getJoinDate(), null),
                employee.getPosition()
        ));
        positionHistoryRepository.save(positionHistory);

        requestOrganizationChartCacheInvalidation();

        return id;
    }

    @Override
    public Long updateInfo(Long id, EmployeeUpdateCommand command) {
        return doUpdateInfo(id, command);
    }

    @Override
    public Long updateInfo(CurrentActor actor, Long id, EmployeeUpdateCommand command) {
        Employee employee = find(id);
        validateCanUpdate(actor, employee, command);
        return doUpdateInfo(employee, command);
    }

    private Long doUpdateInfo(Long id, EmployeeUpdateCommand command) {
        Employee employee = find(id);
        return doUpdateInfo(employee, command);
    }

    private Long doUpdateInfo(Employee employee, EmployeeUpdateCommand command) {
        boolean departmentChanged = isDepartmentChanged(employee.getDepartmentId(), command.departmentId());

        validateDepartmentExistsForUpdate(employee.getDepartmentId(), command.departmentId());
        validateDuplicateEmailForUpdate(employee.getEmail().address(), command.email());

        employee.updateInfo(
                command.departmentId(),
                command.name(),
                command.email(),
                command.joinDate(),
                command.birthDate(),
                command.position(),
                command.type(),
                command.grade(),
                command.avatar(),
                command.memo());

        Long updatedEmployeeId = employeeRepository.save(employee).getIdOrThrow();

        requestOrganizationChartCacheInvalidation();
        if (departmentChanged) {
            invalidateEmployeeSession(updatedEmployeeId);
        }

        return updatedEmployeeId;
    }

    @Override
    public void resign(Long id, LocalDate resignationDate) {
        doResign(id, resignationDate);
    }

    @Override
    public void resign(CurrentActor actor, Long id, LocalDate resignationDate) {
        validateCanManage(actor, find(id), "직원 변경 권한 범위를 벗어났습니다.");
        doResign(id, resignationDate);
    }

    private void doResign(Long id, LocalDate resignationDate) {
        Employee employee = find(id);

        employee.resign(resignationDate);

        employeeRepository.save(employee);

        requestOrganizationChartCacheInvalidation();
    }

    @Override
    public void takeLeave(Long id) {
        doTakeLeave(id);
    }

    @Override
    public void takeLeave(CurrentActor actor, Long id) {
        validateCanManage(actor, find(id), "직원 변경 권한 범위를 벗어났습니다.");
        doTakeLeave(id);
    }

    private void doTakeLeave(Long id) {
        Employee employee = find(id);

        employee.takeLeave();

        employeeRepository.save(employee);

        requestOrganizationChartCacheInvalidation();
    }

    @Override
    public void activate(Long id) {
        doActivate(id);
    }

    @Override
    public void activate(CurrentActor actor, Long id) {
        validateCanManage(actor, find(id), "직원 변경 권한 범위를 벗어났습니다.");
        doActivate(id);
    }

    private void doActivate(Long id) {
        Employee employee = find(id);

        employee.activate();

        employeeRepository.save(employee);

        requestOrganizationChartCacheInvalidation();
    }

    @Override
    public void promote(Long id, EmployeePosition newPosition, @Nullable EmployeeGrade newGrade) {
        doPromote(id, newPosition, newGrade);
    }

    @Override
    public void promote(CurrentActor actor, Long id, EmployeePosition newPosition, @Nullable EmployeeGrade newGrade) {
        validateCanManage(actor, find(id), "직원 변경 권한 범위를 벗어났습니다.");
        doPromote(id, newPosition, newGrade);
    }

    private void doPromote(Long id, EmployeePosition newPosition, @Nullable EmployeeGrade newGrade) {
        Employee employee = find(id);

        employee.promote(newPosition, newGrade);

        employeeRepository.save(employee);

        /**
         * 직급 이력 생성 추가
         * 추후에 도메인 이벤트 방식으로 변경
         */
        PositionHistory positionHistory = PositionHistory.create(new PositionHistoryCreateRequest(
                employee.getIdOrThrow(),
                new Period(LocalDate.now(), null),
                employee.getPosition()
        ));
        positionHistoryRepository.save(positionHistory);

        requestOrganizationChartCacheInvalidation();
    }

    @Override
    public void promote(Long id, EmployeePosition newPosition, @Nullable EmployeeGrade newGrade, LocalDate promotedDate) {
        Employee employee = find(id);

        employee.promote(newPosition, newGrade);

        employeeRepository.save(employee);

        /**
         * 직급 이력 생성 추가
         * 추후에 도메인 이벤트 방식으로 변경
         */
        PositionHistory positionHistory = PositionHistory.create(new PositionHistoryCreateRequest(
                employee.getIdOrThrow(),
                new Period(promotedDate, null),
                employee.getPosition()
        ));
        positionHistoryRepository.save(positionHistory);

        requestOrganizationChartCacheInvalidation();
    }

    @Override
    public void delete(Long id, @Nullable Long deleteBy) {
        doDelete(id, deleteBy);
    }

    @Override
    public void delete(CurrentActor actor, Long id, @Nullable Long deleteBy) {
        validateCanManage(actor, find(id), "직원 변경 권한 범위를 벗어났습니다.");
        doDelete(id, deleteBy);
    }

    private void doDelete(Long id, @Nullable Long deleteBy) {
        Employee employee = find(id);

        employee.softDelete(deleteBy);

        employeeRepository.save(employee);

        requestOrganizationChartCacheInvalidation();
        invalidateEmployeeSession(employee.getIdOrThrow());
    }

    @Override
    public void restore(Long id) {
        doRestore(id);
    }

    @Override
    public void restore(CurrentActor actor, Long id) {
        validateCanManage(actor, findIncludeDeleted(id), "직원 변경 권한 범위를 벗어났습니다.");
        doRestore(id);
    }

    private void doRestore(Long id) {
        Employee employee = findIncludeDeleted(id);

        employee.restore();

        employeeRepository.save(employee);

        requestOrganizationChartCacheInvalidation();
        invalidateEmployeeSession(employee.getIdOrThrow());
    }

    private void requestOrganizationChartCacheInvalidation() {
        eventPublisher.publishEvent(new OrganizationChartInvalidationRequestedEvent());
    }

    private void invalidateEmployeeSession(Long employeeId) {
        accountRepository.findByEmployeeIdAndDeletedFalse(employeeId)
                .ifPresent(account -> sessionInvalidator.invalidateSessions(java.util.List.of(account.getIdOrThrow())));
    }

    private Employee find(Long id) {
        return employeeRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EmployeeNotFoundException("존재하지 않는 직원입니다: " + id));
    }

    private Employee findIncludeDeleted(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("존재하지 않는 직원입니다: " + id));
    }

    private void validateDepartmentExistsForUpdate(Long currentDepartmentId, Long newDepartmentId) {
        if (isDepartmentChanged(currentDepartmentId, newDepartmentId)) {
            validateDepartmentExists(newDepartmentId);
        }
    }

    private void validateDepartmentExists(Long departmentId) {
        if (!departmentRepository.existsByIdAndDeletedFalse(departmentId)) {
            throw new DepartmentNotFoundException("존재하지 않는 부서입니다: " + departmentId);
        }
    }

    private void validateDuplicateEmail(String email) {
        if (employeeRepository.existsByEmail(new Email(email))) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다: " + email);
        }
    }

    private void validateDuplicateEmailForUpdate(String currentEmail, String newEmail) {
        if (isEmailChanged(currentEmail, newEmail)) {
            if (employeeRepository.existsByEmail(new Email(newEmail))) {
                throw new DuplicateEmailException("이미 존재하는 이메일입니다: " + newEmail);
            }
        }
    }

    private boolean isDepartmentChanged(Long currentDepartmentId, Long newDepartmentId) {
        return !currentDepartmentId.equals(newDepartmentId);
    }

    private boolean isEmailChanged(String currentEmail, String newEmail) {
        return !currentEmail.equals(newEmail);
    }

    private void validateCanUpdate(CurrentActor actor, Employee target, EmployeeUpdateCommand command) {
        Set<PermissionScope> scopes = permissionSupport.requirePermission(
                actor,
                EMPLOYEE_WRITE_PERMISSION_CODE,
                "직원 수정 권한 범위를 벗어났습니다."
        );
        if (scopes.contains(PermissionScope.ALL)) {
            return;
        }
        if (scopes.contains(PermissionScope.SELF) && actor.employeeId() != null && actor.employeeId().equals(target.getIdOrThrow())) {
            if (!isAllowedSelfProfileUpdate(target, command)) {
                throw new AccessDeniedException("본인 프로필은 이름, 생년월일, 아바타만 수정할 수 있습니다.");
            }
            return;
        }

        Set<Long> allowedDepartmentIds = permissionSupport.resolveAllowedDepartmentIds(actor, scopes);
        if (!allowedDepartmentIds.contains(target.getDepartmentId())) {
            throw new AccessDeniedException("직원 수정 권한 범위를 벗어났습니다.");
        }
        if (!allowedDepartmentIds.contains(command.departmentId())) {
            throw new AccessDeniedException("직원 배치 권한 범위를 벗어났습니다.");
        }
    }

    private void validateCanManage(CurrentActor actor, Employee target, String message) {
        permissionSupport.validateDepartmentAccess(actor, EMPLOYEE_WRITE_PERMISSION_CODE, target.getDepartmentId(), message);
    }

    private void validateCanCreate(CurrentActor actor, Long departmentId, String permissionCode, String message) {
        permissionSupport.validateDepartmentAccess(actor, permissionCode, departmentId, message);
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
