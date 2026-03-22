package kr.co.abacus.abms.application.department.dto;

import java.time.LocalDate;

import kr.co.abacus.abms.domain.shared.Money;

public record DepartmentRevenueSummary(
        LocalDate targetMonth,
        Money revenue,
        Money cost,
        Money profit) {

}
