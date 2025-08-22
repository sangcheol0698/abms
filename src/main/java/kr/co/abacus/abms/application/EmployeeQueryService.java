package kr.co.abacus.abms.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.adapter.integration.EmployeeRepository;
import kr.co.abacus.abms.application.provided.EmployeeFinder;
import kr.co.abacus.abms.domain.employee.Employee;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class EmployeeQueryService implements EmployeeFinder {

    private final EmployeeRepository employeeRepository;

    @Override
    public Employee find(UUID id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 직원입니다: " + id));
    }

}
