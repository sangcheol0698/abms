package kr.co.abacus.abms.adapter.webapi.department.dto;

import java.util.UUID;

import kr.co.abacus.abms.domain.employee.EmployeePosition;

public record LeaderResponse(
    UUID employeeId,
    String employeeName,
    String position
) {

    public static LeaderResponse of(UUID employeeId, String employeeName, EmployeePosition position) {
        return new LeaderResponse(employeeId, employeeName, position.getDescription());
    }

}
