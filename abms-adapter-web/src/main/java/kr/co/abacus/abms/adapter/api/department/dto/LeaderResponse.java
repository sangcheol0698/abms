package kr.co.abacus.abms.adapter.api.department.dto;

import kr.co.abacus.abms.adapter.api.common.EnumResponse;
import kr.co.abacus.abms.domain.employee.EmployeePosition;

public record LeaderResponse(
        Long employeeId,
        String employeeName,
        EnumResponse position) {

    public static LeaderResponse of(Long employeeId, String employeeName, EmployeePosition position) {
        return new LeaderResponse(
                employeeId,
                employeeName,
                new EnumResponse(position.name(), position.getDescription(), position.getLevel()));
    }

}
