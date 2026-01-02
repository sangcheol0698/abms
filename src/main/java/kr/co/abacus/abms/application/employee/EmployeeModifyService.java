package kr.co.abacus.abms.application.employee;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.application.positionhistory.dto.PositionHistoryCreateRequest;
import kr.co.abacus.abms.application.positionhistory.outbound.PositionHistoryRepository;
import kr.co.abacus.abms.domain.positionhistory.PositionHistory;
import kr.co.abacus.abms.domain.shared.Period;
import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.dto.EmployeeCreateCommand;
import kr.co.abacus.abms.application.employee.dto.EmployeeUpdateCommand;
import kr.co.abacus.abms.application.employee.inbound.EmployeeManager;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.domain.department.DepartmentNotFoundException;
import kr.co.abacus.abms.domain.employee.DuplicateEmailException;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeNotFoundException;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.shared.Email;

@RequiredArgsConstructor
@Transactional
@Service
public class EmployeeModifyService implements EmployeeManager {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionHistoryRepository positionHistoryRepository;

    @Override
    public Long create(EmployeeCreateCommand command) {
        validateDepartmentExists(command.departmentId());
        validateDuplicateEmail(command.email());

        Employee employee = command.toEntity();

        Long id = employeeRepository.save(employee).getId();

        /**
         * 직급 이력 생성 추가
         * 추후에 도메인 이벤트 방식으로 변경
         */
        PositionHistory positionHistory = PositionHistory.create(new PositionHistoryCreateRequest(
            employee.getId(),
            new Period(employee.getCreatedAt().toLocalDate(), null),
            employee.getPosition()
        ));
        positionHistoryRepository.save(positionHistory);

        return id;
    }

    @Override
    public Long updateInfo(Long id, EmployeeUpdateCommand command) {
        Employee employee = find(id);

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

        return employeeRepository.save(employee).getId();
    }

    @Override
    public void resign(Long id, LocalDate resignationDate) {
        Employee employee = find(id);

        employee.resign(resignationDate);

        employeeRepository.save(employee);
    }

    @Override
    public void takeLeave(Long id) {
        Employee employee = find(id);

        employee.takeLeave();

        employeeRepository.save(employee);
    }

    @Override
    public void activate(Long id) {
        Employee employee = find(id);

        employee.activate();

        employeeRepository.save(employee);
    }

    @Override
    public void promote(Long id, EmployeePosition newPosition) {
        Employee employee = find(id);

        employee.promote(newPosition);

        employeeRepository.save(employee);
    }

    @Override
    public void delete(Long id, String deleteBy) {
        Employee employee = find(id);

        employee.softDelete(deleteBy);

        employeeRepository.save(employee);
    }

    @Override
    public void restore(Long id) {
        Employee employee = findIncludeDeleted(id);

        employee.restore();

        employeeRepository.save(employee);
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

}
