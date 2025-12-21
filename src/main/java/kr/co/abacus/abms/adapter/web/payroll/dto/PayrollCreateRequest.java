package kr.co.abacus.abms.adapter.web.payroll.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record PayrollCreateRequest(
        @NotNull Long employeeId,
        @NotNull BigDecimal annualSalary,
        @NotNull LocalDate startDate) {

}
