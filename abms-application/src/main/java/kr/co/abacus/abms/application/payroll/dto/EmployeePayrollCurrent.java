package kr.co.abacus.abms.application.payroll.dto;

import java.time.LocalDate;

public record EmployeePayrollCurrent(
        Long employeeId,
        Long annualSalary,
        Long monthlySalary,
        LocalDate startDate,
        String status
) {

}
