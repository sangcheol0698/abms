package kr.co.abacus.abms.application.employee.provided;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.Valid;

import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeCreateRequest;
import kr.co.abacus.abms.domain.employee.EmployeeUpdateRequest;

/**
 * 직원 생성 및 수정
 */
public interface EmployeeCreator {

    Employee create(@Valid EmployeeCreateRequest request);

    Employee updateInfo(UUID id, @Valid EmployeeUpdateRequest request);

    Employee resign(UUID id, LocalDate resignationDate);

    Employee takeLeave(UUID id);

    Employee activate(UUID id);

    Employee delete(UUID id, String deleteBy);

}
