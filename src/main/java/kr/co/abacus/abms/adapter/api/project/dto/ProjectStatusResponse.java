package kr.co.abacus.abms.adapter.api.project.dto;

import kr.co.abacus.abms.domain.project.ProjectStatus;

public record ProjectStatusResponse(
        String name,
        String description) {

    public static ProjectStatusResponse of(ProjectStatus status) {
        return new ProjectStatusResponse(status.name(), status.getDescription());
    }

}
