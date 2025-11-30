package kr.co.abacus.abms.domain.project;

import java.time.LocalDate;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

public record ProjectCreateRequest(
    UUID partyId,
    String code,
    String name,
    @Nullable String description,
    ProjectStatus status,
    Long contractAmount,
    LocalDate startDate,
    LocalDate endDate
) {
}
