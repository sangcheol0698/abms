package kr.co.abacus.abms.adapter.webapi.department;

import java.util.UUID;

import kr.co.abacus.abms.domain.department.Department;

public record DepartmentResponse(
    UUID departmentId,
    String departmentCode,
    String departmentName,
    String departmentType
) {

    public static DepartmentResponse of(Department department) {
        return new DepartmentResponse(
            department.getId(),
            department.getCode(),
            department.getName(),
            department.getType().getDescription()
        );
    }

}
