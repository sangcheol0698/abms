package kr.co.abacus.abms.application.employee.provided;

import java.time.LocalDate;
import java.util.UUID;

import kr.co.abacus.abms.application.employee.dto.EmployeeCreateRequest;
import kr.co.abacus.abms.application.employee.dto.EmployeeUpdateCommand;
import kr.co.abacus.abms.domain.employee.EmployeePosition;

/**
 * 직원 생성 및 수정
 */
public interface EmployeeManager {

    UUID create(EmployeeCreateRequest command);

    UUID updateInfo(UUID id, EmployeeUpdateCommand command);

    void resign(UUID id, LocalDate resignationDate);

    void takeLeave(UUID id);

    void activate(UUID id);

    void promote(UUID id, EmployeePosition newPosition);

    void delete(UUID id, String deleteBy);

    void restore(UUID id);

}
