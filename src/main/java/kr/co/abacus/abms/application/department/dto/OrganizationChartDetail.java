package kr.co.abacus.abms.application.department.dto;

import java.util.List;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.department.DepartmentType;

public record OrganizationChartDetail(
        Long departmentId,
        String departmentName,
        String departmentCode,
        DepartmentType departmentType,
        @Nullable DepartmentLeaderDetail leader,
        int employeeCount,
        List<OrganizationChartDetail> children) {

}
