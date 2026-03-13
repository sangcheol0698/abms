package kr.co.abacus.abms.application.project.dto;

import java.time.LocalDate;
import java.util.List;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.project.ProjectStatus;

public record ProjectSearchCondition(
        @Nullable String name,
        @Nullable List<ProjectStatus> statuses,
        @Nullable List<Long> partyIds,
        @Nullable LocalDate periodStart,
        @Nullable LocalDate periodEnd,
        @Nullable List<Long> accessibleProjectIds,
        @Nullable List<Long> accessibleLeadDepartmentIds) {

    public ProjectSearchCondition(
            @Nullable String name,
            @Nullable List<ProjectStatus> statuses,
            @Nullable List<Long> partyIds,
            @Nullable LocalDate periodStart,
            @Nullable LocalDate periodEnd
    ) {
        this(name, statuses, partyIds, periodStart, periodEnd, null, null);
    }

    public ProjectSearchCondition withAccess(
            @Nullable List<Long> accessibleProjectIds,
            @Nullable List<Long> accessibleLeadDepartmentIds
    ) {
        return new ProjectSearchCondition(
                name,
                statuses,
                partyIds,
                periodStart,
                periodEnd,
                accessibleProjectIds,
                accessibleLeadDepartmentIds
        );
    }

}
