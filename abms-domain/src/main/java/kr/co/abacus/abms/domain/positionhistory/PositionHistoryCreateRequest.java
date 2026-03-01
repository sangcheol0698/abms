package kr.co.abacus.abms.domain.positionhistory;

import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.shared.Period;

public record PositionHistoryCreateRequest(Long employeeId, Period period, EmployeePosition position) {

}
