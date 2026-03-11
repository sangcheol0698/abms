package kr.co.abacus.abms.adapter.api.project.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.project.dto.ProjectCreateCommand;
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
        @Nullable LocalDate endDate) {

    public ProjectCreateCommand toCommand() {
        return new ProjectCreateCommand(
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
