package kr.co.abacus.abms.adapter.api.project.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.project.Project;

public record ProjectResponse(
        Long projectId,
        Long partyId,
        String partyName,
        String code,
        String name,
        @Nullable String description,
        String status,
        String statusDescription,
        Long contractAmount,
        LocalDate startDate,
        @Nullable LocalDate endDate) {

    public static ProjectResponse from(Project project, String partyName) {
        return new ProjectResponse(
                project.getId(),
                project.getPartyId(),
                partyName,
                project.getCode(),
                project.getName(),
                project.getDescription(),
                project.getStatus().name(),
                project.getStatus().getDescription(),
                project.getContractAmount().amount().longValue(),
                project.getPeriod().startDate(),
                project.getPeriod().endDate());
    }

}
