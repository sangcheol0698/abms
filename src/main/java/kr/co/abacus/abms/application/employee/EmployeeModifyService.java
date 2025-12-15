package kr.co.abacus.abms.application.employee;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.department.required.DepartmentRepository;
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

    @Override
    public UUID create(EmployeeCreateCommand command) {
        validateDepartmentExists(command.departmentId());
        validateDuplicateEmail(command.email());

        Employee employee = command.toEntity();

        return employeeRepository.save(employee).getId();
    }

    @Override
    public UUID updateInfo(UUID id, EmployeeUpdateCommand command) {
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
            command.memo()
        );

        return employeeRepository.save(employee).getId();
    }

    @Override
    public void resign(UUID id, LocalDate resignationDate) {
        Employee employee = find(id);

        employee.resign(resignationDate);

        employeeRepository.save(employee);
    }

    @Override
    public void takeLeave(UUID id) {
        Employee employee = find(id);

        employee.takeLeave();

        employeeRepository.save(employee);
    }

    @Override
    public void activate(UUID id) {
        Employee employee = find(id);

        employee.activate();

        employeeRepository.save(employee);
    }

    @Override
    public void promote(UUID id, EmployeePosition newPosition) {
        Employee employee = find(id);

        employee.promote(newPosition);

        employeeRepository.save(employee);
    }

    @Override
    public void delete(UUID id, String deleteBy) {
        Employee employee = find(id);

        employee.softDelete(deleteBy);

        employeeRepository.save(employee);
    }

    @Override
    public void restore(UUID id) {
        Employee employee = findIncludeDeleted(id);

        employee.restore();

        employeeRepository.save(employee);
    }

    private Employee find(UUID id) {
        return employeeRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new EmployeeNotFoundException("존재하지 않는 직원입니다: " + id));
    }

    private Employee findIncludeDeleted(UUID id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("존재하지 않는 직원입니다: " + id));
    }

    private void validateDepartmentExistsForUpdate(UUID currentDepartmentId, UUID newDepartmentId) {
        if (isDepartmentChanged(currentDepartmentId, currentDepartmentId)) {
            validateDepartmentExists(newDepartmentId);
        }
    }

    private void validateDepartmentExists(UUID departmentId) {
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

    private boolean isDepartmentChanged(UUID currentDepartmentId, UUID newDepartmentId) {
        return !currentDepartmentId.equals(newDepartmentId);

    }

    private boolean isEmailChanged(String currentEmail, String newEmail) {
        return !currentEmail.equals(newEmail);
    }

}
