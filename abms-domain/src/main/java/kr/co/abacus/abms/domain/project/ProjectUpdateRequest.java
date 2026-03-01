package kr.co.abacus.abms.domain.project;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

public record ProjectUpdateRequest(
        Long partyId,
        Long leadDepartmentId,
        String name,
        @Nullable String description,
        ProjectStatus status,
        Long contractAmount,
        LocalDate startDate,
        @Nullable LocalDate endDate) {

}
