package kr.co.abacus.abms.adapter.api.project.dto;

import java.time.LocalDate;
import org.jspecify.annotations.Nullable;
import kr.co.abacus.abms.application.project.dto.ProjectDetail;

public record ProjectDetailResponse(
    Long projectId,
    Long partyId,
    String partyName,
    String code,
    String name,
    @Nullable String description,
    String status,
    String statusDescription,
    Long contractAmount,
    LocalDate startDate,
    @Nullable LocalDate endDate,
    @Nullable Long leadDepartmentId,
    @Nullable String leadDepartmentName) {

    public static ProjectDetailResponse of(ProjectDetail detail) {
        return new ProjectDetailResponse(
            detail.projectId(),
            detail.partyId(),
            detail.partyName(),
            detail.code(),
            detail.name(),
            detail.description(),
            detail.status().name(),
            detail.status().getDescription(),
            detail.contractAmount(),
            detail.startDate(),
            detail.endDate(),
            detail.leadDepartmentId(),
            detail.leadDepartmentName());
    }
}