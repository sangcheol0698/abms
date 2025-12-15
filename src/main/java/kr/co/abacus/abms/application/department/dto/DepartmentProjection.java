package kr.co.abacus.abms.application.department.dto;

import java.util.UUID;

import kr.co.abacus.abms.domain.department.DepartmentType;

public record DepartmentProjection(
    UUID departmentId,
    UUID parentId,
    String departmentName,
    String departmentCode,
    DepartmentType departmentType,
    DepartmentLeaderInfo leader,
    int employeeCount
) {}
