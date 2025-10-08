package kr.co.abacus.abms.application.employee;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.department.provided.DepartmentFinder;
import kr.co.abacus.abms.application.employee.provided.EmployeeFinder;
import kr.co.abacus.abms.application.employee.provided.EmployeeManager;
import kr.co.abacus.abms.application.employee.required.EmployeeRepository;
import kr.co.abacus.abms.domain.employee.DuplicateEmailException;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeCreateRequest;
import kr.co.abacus.abms.domain.employee.EmployeeUpdateRequest;
import kr.co.abacus.abms.domain.shared.Email;

@RequiredArgsConstructor
@Validated
@Transactional
@Service
public class EmployeeModifyService implements EmployeeManager {

    private final EmployeeFinder employeeFinder;
    private final EmployeeRepository employeeRepository;
    private final DepartmentFinder departmentFinder;

    @Override
    public Employee create(EmployeeCreateRequest createRequest) {
        validateDepartmentExists(createRequest.departmentId());
        checkDuplicateEmail(createRequest.email());

        Employee employee = Employee.create(createRequest);

        return employeeRepository.save(employee);
    }

    @Override
    public Employee updateInfo(UUID id, EmployeeUpdateRequest updateRequest) {
        Employee employee = employeeFinder.find(id);

        if (isDepartmentChanged(updateRequest, employee)) {
            validateDepartmentExists(updateRequest.departmentId());
        }

        if (isEmailChanged(updateRequest, employee)) {
            checkDuplicateEmailForUpdate(updateRequest.email(), id);
        }

        employee.updateInfo(updateRequest);

        return employeeRepository.save(employee);
    }

    @Override
    public Employee resign(UUID id, LocalDate resignationDate) {
        Employee employee = employeeFinder.find(id);

        employee.resign(resignationDate);

        return employeeRepository.save(employee);
    }

    @Override
    public Employee takeLeave(UUID id) {
        Employee employee = employeeFinder.find(id);

        employee.takeLeave();

        return employeeRepository.save(employee);
    }

    @Override
    public Employee activate(UUID id) {
        Employee employee = employeeFinder.find(id);

        employee.activate();

        return employeeRepository.save(employee);
    }

    @Override
    public Employee delete(UUID id, String deleteBy) {
        Employee employee = employeeFinder.find(id);

        employee.softDelete(deleteBy);

        return employeeRepository.save(employee);
    }

    @Override
    public Employee restore(UUID id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("삭제된 직원을 찾을 수 없습니다: " + id));

        employee.restore();

        return employeeRepository.save(employee);
    }

    private void validateDepartmentExists(UUID departmentId) {
        departmentFinder.find(departmentId);
    }

    private void checkDuplicateEmail(String email) {
        if (employeeRepository.existsByEmail(new Email(email))) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다: " + email);
        }
    }

    private void checkDuplicateEmailForUpdate(String email, UUID employeeId) {
        if (employeeRepository.existsByEmailAndIdNot(new Email(email), employeeId)) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다: " + email);
        }
    }

    private boolean isDepartmentChanged(EmployeeUpdateRequest updateRequest, Employee employee) {
        return !employee.getDepartmentId().equals(updateRequest.departmentId());
    }

    private boolean isEmailChanged(EmployeeUpdateRequest updateRequest, Employee employee) {
        return !employee.getEmail().address().equals(updateRequest.email());
    }

}
