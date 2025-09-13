package kr.co.abacus.abms.application.employee;

import kr.co.abacus.abms.application.employee.provided.EmployeeFinder;
import kr.co.abacus.abms.application.employee.required.EmployeeRepository;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class EmployeeQueryService implements EmployeeFinder {

    private final EmployeeRepository employeeRepository;

    @Override
    public Employee find(UUID id) {
        return employeeRepository.findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new EmployeeNotFoundException("존재하지 않는 직원입니다: " + id));
    }

}
