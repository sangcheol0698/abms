package kr.co.abacus.abms.application.employee.inbound;

import java.time.LocalDate;

import kr.co.abacus.abms.application.employee.dto.EmployeeCreateCommand;
import kr.co.abacus.abms.application.employee.dto.EmployeeUpdateCommand;
import kr.co.abacus.abms.domain.employee.EmployeePosition;

/**
 * 직원 생성 및 수정
 */
public interface EmployeeManager {

    Long create(EmployeeCreateCommand command);

    Long updateInfo(Long id, EmployeeUpdateCommand command);

    void resign(Long id, LocalDate resignationDate);

    void takeLeave(Long id);

    void activate(Long id);

    void promote(Long id, EmployeePosition newPosition);

    void delete(Long id, String deleteBy);

    void restore(Long id);

}
