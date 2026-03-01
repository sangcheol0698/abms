package kr.co.abacus.abms.application.department.dto;

import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.EmployeePosition;

public record DepartmentProjection(
        Long departmentId,
        Long parentId,
        String departmentName,
        String departmentCode,
        DepartmentType departmentType,
        Long leaderEmployeeId,
        String leaderEmployeeName,
        EmployeePosition leaderEmployeePosition,
        int employeeCount) {

}
