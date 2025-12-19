package kr.co.abacus.abms.adapter.web.department.dto;

import java.util.UUID;

import kr.co.abacus.abms.adapter.web.EnumResponse;
import kr.co.abacus.abms.domain.employee.EmployeePosition;

public record LeaderResponse(
    UUID employeeId,
    String employeeName,
    EnumResponse position
) {

    public static LeaderResponse of(UUID employeeId, String employeeName, EmployeePosition position) {
        return new LeaderResponse(
            employeeId,
            employeeName,
            new EnumResponse(position.name(), position.getDescription())
        );
    }

}
