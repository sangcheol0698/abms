package kr.co.abacus.abms.application.payroll.inbound;

import java.time.LocalDate;

import kr.co.abacus.abms.domain.shared.Money;

public interface PayrollManager {

    void changeSalary(Long employeeId, Money annualSalary, LocalDate startDate);

}
