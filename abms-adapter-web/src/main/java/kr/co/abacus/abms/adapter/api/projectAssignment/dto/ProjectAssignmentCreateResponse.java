package kr.co.abacus.abms.adapter.api.projectAssignment.dto;

public record ProjectAssignmentCreateResponse(Long id) {
    public static ProjectAssignmentCreateResponse of(Long id) {
        return new ProjectAssignmentCreateResponse(id);
    }
}