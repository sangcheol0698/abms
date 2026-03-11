package kr.co.abacus.abms.adapter.api.project.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.project.dto.ProjectUpdateCommand;
import kr.co.abacus.abms.domain.project.ProjectStatus;

public record ProjectUpdateApiRequest(
        Long partyId,
        Long leadDepartmentId,
        String name,
        String description,
        String status,
        Long contractAmount,
        LocalDate startDate,
        @Nullable LocalDate endDate) {

    public ProjectUpdateCommand toCommand() {
        return new ProjectUpdateCommand(
                partyId,
                leadDepartmentId,
                name,
                description,
                ProjectStatus.valueOf(status),
                contractAmount,
                startDate,
                endDate);
    }

}
