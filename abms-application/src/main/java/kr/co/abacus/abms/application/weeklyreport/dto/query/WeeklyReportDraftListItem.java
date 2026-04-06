package kr.co.abacus.abms.application.weeklyreport.dto.query;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.weeklyreport.WeeklyReportStatus;

public record WeeklyReportDraftListItem(
        Long id,
        String title,
        LocalDate weekStart,
        LocalDate weekEnd,
        WeeklyReportStatus status,
        String reportMarkdown,
        WeeklyReportSnapshotSummary snapshotSummary,
        @Nullable String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

}
