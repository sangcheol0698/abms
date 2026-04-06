package kr.co.abacus.abms.adapter.api.weeklyreport.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportDraftDetail;

public record WeeklyReportDraftDetailResponse(
        Long id,
        String title,
        LocalDate weekStart,
        LocalDate weekEnd,
        String status,
        String statusDescription,
        String reportMarkdown,
        WeeklyReportSnapshotSummaryResponse snapshotSummary,
        String snapshotJson,
        @Nullable String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static WeeklyReportDraftDetailResponse from(WeeklyReportDraftDetail detail) {
        return new WeeklyReportDraftDetailResponse(
                detail.id(),
                detail.title(),
                detail.weekStart(),
                detail.weekEnd(),
                detail.status().name(),
                detail.status().getDescription(),
                detail.reportMarkdown(),
                WeeklyReportSnapshotSummaryResponse.from(detail.snapshotSummary()),
                detail.snapshotJson(),
                detail.failureReason(),
                detail.createdAt(),
                detail.updatedAt()
        );
    }
}
