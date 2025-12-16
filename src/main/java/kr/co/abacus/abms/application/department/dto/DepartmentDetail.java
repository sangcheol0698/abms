package kr.co.abacus.abms.application.department.dto;

import java.util.UUID;

import kr.co.abacus.abms.domain.department.DepartmentType;

public record DepartmentDetail(
    UUID id,
    String code,
    String name,
    DepartmentType type,
    UUID leaderEmployeeId,
    String leaderEmployeeName,
    UUID parentDepartmentId,
    String parentDepartmentName,
    int employeeCount
) {

}
