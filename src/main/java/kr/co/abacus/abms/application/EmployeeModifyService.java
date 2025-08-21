package kr.co.abacus.abms.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.adapter.integration.EmployeeRepository;
import kr.co.abacus.abms.application.provided.EmployeeCreator;
import kr.co.abacus.abms.domain.employee.DuplicateEmailException;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeCreateRequest;
import kr.co.abacus.abms.domain.shared.Email;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional
@Service
public class EmployeeModifyService implements EmployeeCreator {

    private final EmployeeRepository employeeRepository;

    @Override
    public Employee create(EmployeeCreateRequest request) {
        checkDuplicateEmail(request);

        Employee employee = Employee.create(request);
        employeeRepository.save(employee);

        return employee;
    }

    private void checkDuplicateEmail(EmployeeCreateRequest request) {
        if (employeeRepository.existsByEmail(new Email(request.email()))) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다: " + request.email());
        }
    }

}
