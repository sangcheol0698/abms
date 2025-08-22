package kr.co.abacus.abms.application;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import kr.co.abacus.abms.application.required.EmployeeRepository;
import kr.co.abacus.abms.application.provided.EmployeeCreator;
import kr.co.abacus.abms.application.provided.EmployeeFinder;
import kr.co.abacus.abms.domain.employee.DuplicateEmailException;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeCreateRequest;
import kr.co.abacus.abms.domain.employee.EmployeeUpdateRequest;
import kr.co.abacus.abms.domain.shared.Email;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Validated
@Transactional
@Service
public class EmployeeModifyService implements EmployeeCreator {

    private final EmployeeFinder employeeFinder;
    private final EmployeeRepository employeeRepository;

    @Override
    public Employee create(EmployeeCreateRequest createRequest) {
        checkDuplicateEmail(createRequest.email());

        Employee employee = Employee.create(createRequest);

        return employeeRepository.save(employee);
    }

    @Override
    public Employee updateInfo(UUID id, EmployeeUpdateRequest updateRequest) {
        Employee employee = employeeFinder.find(id);

        if (isEmailChanged(updateRequest, employee)) checkDuplicateEmail(updateRequest.email());

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

    private void checkDuplicateEmail(String email) {
        if (employeeRepository.existsByEmail(new Email(email))) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다: " + email);
        }
    }

    private static boolean isEmailChanged(EmployeeUpdateRequest updateRequest, Employee employee) {
        return !employee.getEmail().address().equals(updateRequest.email());
    }

}
