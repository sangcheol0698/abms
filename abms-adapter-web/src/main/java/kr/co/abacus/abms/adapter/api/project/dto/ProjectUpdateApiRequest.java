package kr.co.abacus.abms.adapter.api.project.dto;

import java.time.LocalDate;

import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.domain.project.ProjectUpdateRequest;

public record ProjectUpdateApiRequest(
        Long partyId,
        Long leadDepartmentId,
        String name,
        String description,
        String status,
        Long contractAmount,
        LocalDate startDate,
        LocalDate endDate) {

    public ProjectUpdateRequest toDomainRequest() {
        return new ProjectUpdateRequest(
                partyId, // Added partyId
                leadDepartmentId,
                name,
                description,
                ProjectStatus.valueOf(status), // Conversion added
                contractAmount,
                startDate,
                endDate);
    }

}
