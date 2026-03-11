package kr.co.abacus.abms.application.project.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.project.ProjectStatus;

public record ProjectUpdateCommand(
        Long partyId,
        Long leadDepartmentId,
        String name,
        @Nullable String description,
        ProjectStatus status,
        Long contractAmount,
        LocalDate startDate,
        @Nullable LocalDate endDate) {

}
