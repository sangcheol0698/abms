package kr.co.abacus.abms.application.payroll.inbound;

import java.time.LocalDate;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.domain.shared.Money;

public interface PayrollManager {

    void changeSalary(Long employeeId, Money annualSalary, LocalDate startDate);

    void changeSalary(CurrentActor actor, Long employeeId, Money annualSalary, LocalDate startDate);

}
