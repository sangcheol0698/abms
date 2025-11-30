package kr.co.abacus.abms.adapter.webapi.project.dto;

import java.time.LocalDate;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.domain.project.ProjectUpdateRequest;

public record ProjectUpdateApiRequest(
        UUID partyId, // Added partyId
        String name,
        String description, // @Nullable removed as per instruction's code edit
        String status, // Type changed from ProjectStatus to String
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
