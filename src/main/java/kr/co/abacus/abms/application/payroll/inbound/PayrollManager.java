package kr.co.abacus.abms.application.payroll.inbound;

import java.time.LocalDate;
import java.util.UUID;

import kr.co.abacus.abms.domain.shared.Money;

public interface PayrollManager {

    void changeSalary(UUID employeeId, Money annualSalary, LocalDate startDate);

}
