package kr.co.abacus.abms.domain.weeklyreport;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.AbstractEntity;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_weekly_report_draft")
public class WeeklyReportDraft extends AbstractEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false)
    private LocalDate weekStart;

    @Column(nullable = false)
    private LocalDate weekEnd;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WeeklyReportScopeType scopeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WeeklyReportStatus status;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reportMarkdown;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String snapshotJson;

    @Column(nullable = false, length = 100)
    private String modelName;

    @Column(nullable = false, length = 30)
    private String promptVersion;

    @Column(nullable = false)
    private Long createdByAccountId;

    @Nullable
    @Column(length = 500)
    private String failureReason;

    private WeeklyReportDraft(
            String title,
            LocalDate weekStart,
            LocalDate weekEnd,
            WeeklyReportScopeType scopeType,
            WeeklyReportStatus status,
            String reportMarkdown,
            String snapshotJson,
            String modelName,
            String promptVersion,
            Long createdByAccountId,
            @Nullable String failureReason
    ) {
        this.title = requireNonNull(title);
        this.weekStart = requireNonNull(weekStart);
        this.weekEnd = requireNonNull(weekEnd);
        this.scopeType = requireNonNull(scopeType);
        this.status = requireNonNull(status);
        this.reportMarkdown = requireNonNull(reportMarkdown);
        this.snapshotJson = requireNonNull(snapshotJson);
        this.modelName = requireNonNull(modelName);
        this.promptVersion = requireNonNull(promptVersion);
        this.createdByAccountId = requireNonNull(createdByAccountId);
        this.failureReason = failureReason;
    }

    public static WeeklyReportDraft createPending(
            String title,
            LocalDate weekStart,
            LocalDate weekEnd,
            String modelName,
            String promptVersion,
            Long createdByAccountId
    ) {
        return new WeeklyReportDraft(
                title,
                weekStart,
                weekEnd,
                WeeklyReportScopeType.GLOBAL_OPS,
                WeeklyReportStatus.PENDING,
                "",
                "",
                modelName,
                promptVersion,
                createdByAccountId,
                null
        );
    }

    public void markCollecting() {
        if (status == WeeklyReportStatus.CANCELLED) {
            return;
        }
        this.status = WeeklyReportStatus.COLLECTING;
        this.failureReason = null;
    }

    public void markGenerating() {
        if (status == WeeklyReportStatus.CANCELLED) {
            return;
        }
        this.status = WeeklyReportStatus.GENERATING;
        this.failureReason = null;
    }

    public void complete(String reportMarkdown, String snapshotJson) {
        if (status == WeeklyReportStatus.CANCELLED) {
            return;
        }
        this.status = WeeklyReportStatus.DRAFT;
        this.reportMarkdown = requireNonNull(reportMarkdown);
        this.snapshotJson = requireNonNull(snapshotJson);
        this.failureReason = null;
    }

    public void fail(String failureReason) {
        if (status == WeeklyReportStatus.CANCELLED) {
            return;
        }
        this.status = WeeklyReportStatus.FAILED;
        this.failureReason = requireNonNull(failureReason);
    }

    public void cancel() {
        this.status = WeeklyReportStatus.CANCELLED;
        this.failureReason = null;
    }

    public boolean isRunning() {
        return status == WeeklyReportStatus.PENDING
                || status == WeeklyReportStatus.COLLECTING
                || status == WeeklyReportStatus.GENERATING;
    }
}
