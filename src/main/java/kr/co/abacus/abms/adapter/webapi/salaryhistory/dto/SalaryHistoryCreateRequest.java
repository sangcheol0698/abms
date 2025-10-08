package kr.co.abacus.abms.adapter.webapi.salaryhistory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record SalaryHistoryCreateRequest(
    @NotNull UUID employeeId,
    @NotNull BigDecimal annualSalary,
    @NotNull LocalDate startDate
) {

}
