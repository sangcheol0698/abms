package kr.co.abacus.abms.application.department.dto;

import java.util.UUID;

import kr.co.abacus.abms.domain.employee.EmployeePosition;

public record DepartmentLeaderDetail(
    UUID leaderEmployeeId,
    String leaderEmployeeName,
    EmployeePosition leaderPosition
) {

}
