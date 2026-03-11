package kr.co.abacus.abms.adapter.api.project.dto;

public record ProjectUpdateResponse(Long projectId) {

    public static ProjectUpdateResponse of(Long projectId) {
        return new ProjectUpdateResponse(projectId);
    }

}
