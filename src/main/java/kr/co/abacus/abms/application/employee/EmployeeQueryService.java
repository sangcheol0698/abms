package kr.co.abacus.abms.application.employee;

import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.employee.dto.EmployeeDetail;
import kr.co.abacus.abms.application.employee.dto.EmployeeSearchCondition;
import kr.co.abacus.abms.application.employee.dto.EmployeeSummary;
import kr.co.abacus.abms.application.employee.provided.EmployeeFinder;
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
    public @Nullable EmployeeDetail findEmployeeDetail(UUID id) {
        return employeeRepository.findEmployeeDetail(id);
    }

    @Override
    public Page<EmployeeSummary> search(EmployeeSearchCondition request, Pageable pageable) {
        return employeeRepository.search(request, pageable);
    }

}
