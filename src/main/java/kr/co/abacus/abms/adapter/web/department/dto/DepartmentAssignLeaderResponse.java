package kr.co.abacus.abms.adapter.web.department.dto;

import java.util.UUID;

public record DepartmentAssignLeaderResponse(
    UUID departmentId
) {

    public static DepartmentAssignLeaderResponse of(UUID departmentId) {
        return new DepartmentAssignLeaderResponse(departmentId);
    }

}
