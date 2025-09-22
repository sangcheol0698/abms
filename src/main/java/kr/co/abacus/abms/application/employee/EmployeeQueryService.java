package kr.co.abacus.abms.application.employee;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.employee.provided.EmployeeFinder;
import kr.co.abacus.abms.application.employee.provided.EmployeeSearchRequest;
import kr.co.abacus.abms.application.employee.required.EmployeeRepository;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeNotFoundException;

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

    @Override
    public Page<Employee> search(EmployeeSearchRequest request, Pageable pageable) {
        return employeeRepository.search(request, pageable);
    }

}
