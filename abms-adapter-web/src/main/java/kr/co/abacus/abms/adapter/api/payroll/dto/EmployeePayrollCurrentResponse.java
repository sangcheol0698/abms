package kr.co.abacus.abms.adapter.api.payroll.dto;

import java.time.LocalDate;

import kr.co.abacus.abms.application.payroll.dto.EmployeePayrollCurrent;

public record EmployeePayrollCurrentResponse(
        Long employeeId,
        Long annualSalary,
        Long monthlySalary,
        LocalDate startDate,
        String status
) {

    public static EmployeePayrollCurrentResponse from(EmployeePayrollCurrent detail) {
        return new EmployeePayrollCurrentResponse(
                detail.employeeId(),
                detail.annualSalary(),
                detail.monthlySalary(),
                detail.startDate(),
                detail.status()
        );
    }

}
