package kr.co.abacus.abms.application.department.dto;

import java.util.List;

import kr.co.abacus.abms.domain.department.DepartmentType;

public record OrganizationChartDetail(
    Long departmentId,
    String departmentName,
    String departmentCode,
    DepartmentType departmentType,
    DepartmentLeaderDetail leader,
    int employeeCount,
    List<OrganizationChartDetail> children) {

}
