package kr.co.abacus.abms.adapter.api.payroll.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.payroll.dto.EmployeePayrollHistoryItem;

public record EmployeePayrollHistoryResponse(
        Long employeeId,
        Long annualSalary,
        Long monthlySalary,
        LocalDate startDate,
        @Nullable LocalDate endDate,
        String status
) {

    public static EmployeePayrollHistoryResponse from(EmployeePayrollHistoryItem item) {
        return new EmployeePayrollHistoryResponse(
                item.employeeId(),
                item.annualSalary(),
                item.monthlySalary(),
                item.startDate(),
                item.endDate(),
                item.status()
        );
    }

}
