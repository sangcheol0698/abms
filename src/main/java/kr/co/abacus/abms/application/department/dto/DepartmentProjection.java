package kr.co.abacus.abms.application.department.dto;

import java.util.UUID;

import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.EmployeePosition;

public record DepartmentProjection(
    UUID departmentId,
    UUID parentId,
    String departmentName,
    String departmentCode,
    DepartmentType departmentType,
    UUID leaderEmployeeId,
    String leaderEmployeeName,
    EmployeePosition leaderEmployeePosition,
    int employeeCount
) {}
