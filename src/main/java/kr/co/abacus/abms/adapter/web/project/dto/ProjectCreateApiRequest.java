package kr.co.abacus.abms.adapter.web.project.dto;

import java.time.LocalDate;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.project.ProjectCreateRequest;
import kr.co.abacus.abms.domain.project.ProjectStatus;

public record ProjectCreateApiRequest(
    UUID partyId,
    String code,
    String name,
    @Nullable String description,
    ProjectStatus status,
    Long contractAmount,
    LocalDate startDate,
    LocalDate endDate
) {

    public ProjectCreateRequest toDomainRequest() {
        return new ProjectCreateRequest(
            partyId,
            code,
            name,
            description,
            status,
            contractAmount,
            startDate,
            endDate
        );
    }

}
