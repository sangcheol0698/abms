package kr.co.abacus.abms.application.department.dto;

import kr.co.abacus.abms.domain.department.DepartmentType;

public record DepartmentDetail(
        Long id,
        String code,
        String name,
        DepartmentType type,
        Long leaderEmployeeId,
        String leaderEmployeeName,
        Long parentDepartmentId,
        String parentDepartmentName,
        int employeeCount) {

}
