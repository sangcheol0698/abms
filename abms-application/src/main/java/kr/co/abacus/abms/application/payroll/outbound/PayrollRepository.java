package kr.co.abacus.abms.application.payroll.outbound;

import java.time.LocalDate;
import java.util.Optional;

import kr.co.abacus.abms.domain.payroll.Payroll;

public interface PayrollRepository {

    Payroll save(Payroll payroll);

    Optional<Payroll> findCurrentSalaryByEmployeeId(Long employeeId);

    Optional<Payroll> findByEmployeeIdAndTargetDate(Long employeeId, LocalDate targetDate);

}
