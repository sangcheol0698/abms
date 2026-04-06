package kr.co.abacus.abms.application.weeklyreport.dto.query;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.weeklyreport.WeeklyReportStatus;

public record WeeklyReportDraftDetail(
        Long id,
        String title,
        LocalDate weekStart,
        LocalDate weekEnd,
        WeeklyReportStatus status,
        String reportMarkdown,
        WeeklyReportSnapshotSummary snapshotSummary,
        String snapshotJson,
        @Nullable String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

}
