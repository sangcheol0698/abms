package kr.co.abacus.abms.adapter.api.department.dto;

public record DepartmentAssignLeaderResponse(
        Long departmentId) {

    public static DepartmentAssignLeaderResponse of(Long departmentId) {
        return new DepartmentAssignLeaderResponse(departmentId);
    }

}
