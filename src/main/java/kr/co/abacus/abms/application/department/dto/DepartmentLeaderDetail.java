package kr.co.abacus.abms.application.department.dto;

import kr.co.abacus.abms.domain.employee.EmployeePosition;

public record DepartmentLeaderDetail(
    Long leaderEmployeeId,
    String leaderEmployeeName,
    EmployeePosition leaderPosition) {

}
