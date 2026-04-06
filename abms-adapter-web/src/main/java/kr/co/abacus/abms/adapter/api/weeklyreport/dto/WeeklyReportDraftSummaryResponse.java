package kr.co.abacus.abms.adapter.api.weeklyreport.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportDraftListItem;

public record WeeklyReportDraftSummaryResponse(
        Long id,
        String title,
        LocalDate weekStart,
        LocalDate weekEnd,
        String status,
        String statusDescription,
        String reportMarkdown,
        WeeklyReportSnapshotSummaryResponse snapshotSummary,
        @Nullable String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static WeeklyReportDraftSummaryResponse from(WeeklyReportDraftListItem item) {
        return new WeeklyReportDraftSummaryResponse(
                item.id(),
                item.title(),
                item.weekStart(),
                item.weekEnd(),
                item.status().name(),
                item.status().getDescription(),
                item.reportMarkdown(),
                WeeklyReportSnapshotSummaryResponse.from(item.snapshotSummary()),
                item.failureReason(),
                item.createdAt(),
                item.updatedAt()
        );
    }
}
