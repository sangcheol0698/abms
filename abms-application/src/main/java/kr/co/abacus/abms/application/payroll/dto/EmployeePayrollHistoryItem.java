package kr.co.abacus.abms.application.payroll.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

public record EmployeePayrollHistoryItem(
        Long employeeId,
        Long annualSalary,
        Long monthlySalary,
        LocalDate startDate,
        @Nullable LocalDate endDate,
        String status
) {

}
