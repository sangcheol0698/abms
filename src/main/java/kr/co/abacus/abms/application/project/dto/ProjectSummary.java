package kr.co.abacus.abms.application.project.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.domain.shared.Money;

public record ProjectSummary(
        Long projectId,
        Long partyId,
        String code,
        String name,
        @Nullable String description,
        ProjectStatus status,
        Money contractAmount,
        LocalDate startDate,
        @Nullable LocalDate endDate) {

}
