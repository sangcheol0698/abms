package kr.co.abacus.abms.adapter.web.employee.dto;

import kr.co.abacus.abms.domain.employee.EmployeePosition;

public record EmployeePositionResponse(
    String name,
    String description,
    int rank
) {

    public static EmployeePositionResponse of(EmployeePosition position) {
        return new EmployeePositionResponse(position.name(), position.getDescription(), position.getRank());
    }

}
