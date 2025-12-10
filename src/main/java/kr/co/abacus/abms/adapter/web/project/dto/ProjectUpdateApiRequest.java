package kr.co.abacus.abms.adapter.web.project.dto;

import java.time.LocalDate;
import java.util.UUID;

import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.domain.project.ProjectUpdateRequest;

public record ProjectUpdateApiRequest(
        UUID partyId,
        String name,
        String description,
        String status,
        Long contractAmount,
        LocalDate startDate,
        LocalDate endDate) {

    public ProjectUpdateRequest toDomainRequest() {
        return new ProjectUpdateRequest(
                partyId, // Added partyId
                name,
                description,
                ProjectStatus.valueOf(status), // Conversion added
                contractAmount,
                startDate,
                endDate);
    }

}
