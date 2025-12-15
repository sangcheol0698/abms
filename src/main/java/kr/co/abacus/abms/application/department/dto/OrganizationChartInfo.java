package kr.co.abacus.abms.application.department.dto;

import java.util.List;
import java.util.UUID;

import kr.co.abacus.abms.domain.department.DepartmentType;

public record OrganizationChartInfo(
    UUID departmentId,
    String departmentName,
    String departmentCode,
    DepartmentType departmentType,
    DepartmentLeaderInfo leader,
    int employeeCount,
    List<OrganizationChartInfo> children
) {

}
