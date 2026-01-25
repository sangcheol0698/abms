package kr.co.abacus.abms.application.project.dto;

import java.time.LocalDate;
import java.util.List;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.project.ProjectStatus;

public record ProjectSearchCondition(
        @Nullable String name,
        @Nullable List<ProjectStatus> statuses,
        @Nullable List<Long> partyIds,
        @Nullable LocalDate startDate,
        @Nullable LocalDate endDate) {

}
