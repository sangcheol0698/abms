package kr.co.abacus.abms.adapter.web.department.dto;

import java.util.UUID;

public record OrganizationEmployeeResponse(
    UUID employeeId,
    String employeeName,
    String employeePosition
) {

    public static OrganizationEmployeeResponse of(UUID employeeId, String employeeName, String employeePosition) {
        return new OrganizationEmployeeResponse(employeeId, employeeName, employeePosition);
    }

}
