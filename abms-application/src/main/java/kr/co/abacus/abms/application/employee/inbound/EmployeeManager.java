package kr.co.abacus.abms.application.employee.inbound;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.employee.dto.EmployeeCreateCommand;
import kr.co.abacus.abms.application.employee.dto.EmployeeUpdateCommand;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;

/**
 * 직원 생성 및 수정
 */
public interface EmployeeManager {

    Long create(EmployeeCreateCommand command);

    Long create(CurrentActor actor, EmployeeCreateCommand command);

    Long updateInfo(Long id, EmployeeUpdateCommand command);

    Long updateInfo(CurrentActor actor, Long id, EmployeeUpdateCommand command);

    void resign(Long id, LocalDate resignationDate);

    void resign(CurrentActor actor, Long id, LocalDate resignationDate);

    void takeLeave(Long id);

    void takeLeave(CurrentActor actor, Long id);

    void activate(Long id);

    void activate(CurrentActor actor, Long id);

    void promote(Long id, EmployeePosition newPosition, @Nullable EmployeeGrade newGrade);

    void promote(CurrentActor actor, Long id, EmployeePosition newPosition, @Nullable EmployeeGrade newGrade);

    void promote(Long id, EmployeePosition newPosition, @Nullable EmployeeGrade newGrade, LocalDate promotedDate);

    void delete(Long id, @Nullable Long deleteBy);

    void delete(CurrentActor actor, Long id, @Nullable Long deleteBy);

    void restore(Long id);

    void restore(CurrentActor actor, Long id);

}
