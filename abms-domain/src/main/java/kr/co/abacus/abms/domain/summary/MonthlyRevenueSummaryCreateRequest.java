package kr.co.abacus.abms.domain.summary;

import java.time.LocalDate;

import kr.co.abacus.abms.domain.shared.Money;

public record MonthlyRevenueSummaryCreateRequest(
    Long projectId,
    String projectCode,
    String projectName,
    Long leadDepartmentId,
    String leadDepartmentCode,
    String leadDepartmentName,
    LocalDate summaryDate,
    Money revenueAmount,
    Money costAmount,
    Money profitAmount
) {

}
