package kr.co.abacus.abms.adapter.api.project.dto;

public record ProjectCreateResponse(Long projectId) {

    public static ProjectCreateResponse of(Long projectId) {
        return new ProjectCreateResponse(projectId);
    }

}
