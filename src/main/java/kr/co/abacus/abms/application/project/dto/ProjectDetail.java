package kr.co.abacus.abms.application.project.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectStatus;

public record ProjectDetail(
        Long projectId,
        Long partyId,
        String partyName,
        String code,
        String name,
        @Nullable String description,
        ProjectStatus status,
        Long contractAmount,
        LocalDate startDate,
        @Nullable LocalDate endDate,
        @Nullable Long leadDepartmentId,
        @Nullable String leadDepartmentName) {

    public static ProjectDetail of(Project project, String partyName, @Nullable String leadDepartmentName) {
        return new ProjectDetail(
                project.getId(),
                project.getPartyId(),
                partyName,
                project.getCode(),
                project.getName(),
                project.getDescription(),
                project.getStatus(),
                project.getContractAmount().amount().longValue(),
                project.getPeriod().startDate(),
                project.getPeriod().endDate(),
                project.getLeadDepartmentId(),
                leadDepartmentName);
    }

}
