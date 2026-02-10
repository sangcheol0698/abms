package kr.co.abacus.abms.adapter.api.project.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.project.dto.ProjectSummary;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.project.RevenueType;

public record ProjectRevenuePlanResponse(
        Long projectId,
        Integer sequence,
        LocalDate revenueDate,
        RevenueType type,
        Long amount,
        @Nullable String memo
) {

    public static ProjectRevenuePlanResponse from(ProjectRevenuePlan projectRevenuePlan) {
        return new ProjectRevenuePlanResponse(
            projectRevenuePlan.getProjectId(),
            projectRevenuePlan.getSequence(),
            projectRevenuePlan.getRevenueDate(),
            projectRevenuePlan.getType(),
            projectRevenuePlan.getAmount().amount().longValue(),
            projectRevenuePlan.getMemo()
        );
    }

}
