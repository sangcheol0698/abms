package kr.co.abacus.abms.application.department.dto;

import java.util.List;
import java.util.UUID;

import kr.co.abacus.abms.domain.department.DepartmentType;

public record OrganizationChartModel(
    UUID departmentId,
    String departmentName,
    String departmentCode,
    DepartmentType departmentType,
    LeaderModel leader,
    List<OrganizationChartModel> children
) {

}
