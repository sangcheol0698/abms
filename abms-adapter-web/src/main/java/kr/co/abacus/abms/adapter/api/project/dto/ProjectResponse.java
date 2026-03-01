package kr.co.abacus.abms.adapter.api.project.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.project.dto.ProjectSummary;
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
                project.getIdOrThrow(),
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

    public static ProjectResponse from(ProjectSummary summary, String partyName) {
        return new ProjectResponse(
                summary.projectId(),
                summary.partyId(),
                partyName,
                summary.code(),
                summary.name(),
                summary.description(),
                summary.status().name(),
                summary.status().getDescription(),
                summary.contractAmount().amount().longValue(),
                summary.startDate(),
                summary.endDate());
    }

}
