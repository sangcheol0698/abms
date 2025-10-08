package kr.co.abacus.abms.application.salaryhistory.provided;

import java.time.LocalDate;
import java.util.UUID;

import kr.co.abacus.abms.domain.shared.Money;

public interface SalaryHistoryManager {

    void changeSalary(UUID employeeId, Money annualSalary, LocalDate startDate);

}
