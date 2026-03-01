package kr.co.abacus.abms.adapter.api.project.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.project.ProjectCreateRequest;
import kr.co.abacus.abms.domain.project.ProjectStatus;

public record ProjectCreateApiRequest(
        Long partyId,
        Long leadDepartmentId,
        String code,
        String name,
        @Nullable String description,
        ProjectStatus status,
        Long contractAmount,
        LocalDate startDate,
        LocalDate endDate) {

    public ProjectCreateRequest toDomainRequest() {
        return new ProjectCreateRequest(
                partyId,
                leadDepartmentId,
                code,
                name,
                description,
                status,
                contractAmount,
                startDate,
                endDate);
    }

}
