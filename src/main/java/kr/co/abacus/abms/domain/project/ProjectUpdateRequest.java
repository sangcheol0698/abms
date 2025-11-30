package kr.co.abacus.abms.domain.project;

import java.time.LocalDate;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

public record ProjectUpdateRequest(
        UUID partyId,
        String name,
        @Nullable String description,
        ProjectStatus status,
        Long contractAmount,
        LocalDate startDate,
        @Nullable LocalDate endDate) {

}
