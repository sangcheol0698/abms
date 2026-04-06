package kr.co.abacus.abms.application.weeklyreport;

import java.time.LocalDate;
import java.time.ZoneId;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import kr.co.abacus.abms.application.weeklyreport.dto.command.WeeklyReportGenerateCommand;
import kr.co.abacus.abms.application.weeklyreport.dto.command.WeeklyReportUpdateCommand;
import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportDraftDetail;
import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportInsightData;
import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportSnapshot;
import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportSnapshotSummary;
import kr.co.abacus.abms.application.weeklyreport.inbound.WeeklyReportDraftManager;
import kr.co.abacus.abms.application.weeklyreport.outbound.WeeklyReportDraftRepository;
import kr.co.abacus.abms.domain.weeklyreport.WeeklyReportDraft;

@Service
@Transactional
public class WeeklyReportDraftCommandService implements WeeklyReportDraftManager {

    private static final ZoneId SEOUL_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final WeeklyReportAiWriter aiWriter;
    private final WeeklyReportDraftRepository weeklyReportDraftRepository;
    private final WeeklyReportDraftAsyncGenerationService asyncGenerationService;

    public WeeklyReportDraftCommandService(
            WeeklyReportAiWriter aiWriter,
            WeeklyReportDraftRepository weeklyReportDraftRepository,
            WeeklyReportDraftAsyncGenerationService asyncGenerationService
    ) {
        this.aiWriter = aiWriter;
        this.weeklyReportDraftRepository = weeklyReportDraftRepository;
        this.asyncGenerationService = asyncGenerationService;
    }

    @Override
    public WeeklyReportDraftDetail createDraft(Long accountId, WeeklyReportGenerateCommand command) {
        WeeklyReportPeriodResolver.WeekRange weekRange = WeeklyReportPeriodResolver.resolve(
                command.weekStart(),
                command.weekEnd(),
                LocalDate.now(SEOUL_ZONE_ID)
        );
        return createPendingDraft(accountId, weekRange.weekStart(), weekRange.weekEnd());
    }

    @Override
    public WeeklyReportDraftDetail regenerateDraft(Long accountId, Long draftId) {
        WeeklyReportDraft draft = weeklyReportDraftRepository.findByIdAndCreatedByAccountIdAndDeletedFalse(draftId, accountId)
                .orElseThrow(() -> new IllegalArgumentException("주간 보고서 초안을 찾을 수 없습니다: " + draftId));
        return createPendingDraft(accountId, draft.getWeekStart(), draft.getWeekEnd());
    }

    @Override
    public WeeklyReportDraftDetail cancelDraft(Long accountId, Long draftId) {
        WeeklyReportDraft draft = weeklyReportDraftRepository.findByIdAndCreatedByAccountIdAndDeletedFalse(draftId, accountId)
                .orElseThrow(() -> new IllegalArgumentException("주간 보고서 초안을 찾을 수 없습니다: " + draftId));
        if (draft.isRunning()) {
            draft.cancel();
            weeklyReportDraftRepository.save(draft);
            asyncGenerationService.cancelDraft(draftId);
        }
        return toDetail(draft, null);
    }

    @Override
    public WeeklyReportDraftDetail updateDraft(Long accountId, Long draftId, WeeklyReportUpdateCommand command) {
        WeeklyReportDraft draft = weeklyReportDraftRepository.findByIdAndCreatedByAccountIdAndDeletedFalse(draftId, accountId)
                .orElseThrow(() -> new IllegalArgumentException("주간 보고서 초안을 찾을 수 없습니다: " + draftId));
        draft.update(command.title(), command.reportMarkdown());
        WeeklyReportDraft savedDraft = weeklyReportDraftRepository.save(draft);
        return toDetail(savedDraft, null);
    }

    @Override
    public void deleteDraft(Long accountId, Long draftId) {
        WeeklyReportDraft draft = weeklyReportDraftRepository.findByIdAndCreatedByAccountIdAndDeletedFalse(draftId, accountId)
                .orElseThrow(() -> new IllegalArgumentException("주간 보고서 초안을 찾을 수 없습니다: " + draftId));
        if (draft.isRunning()) {
            throw new IllegalStateException("생성 중인 주간 보고서는 삭제할 수 없습니다.");
        }
        draft.softDelete(accountId);
        weeklyReportDraftRepository.save(draft);
    }

    private WeeklyReportDraftDetail createPendingDraft(Long accountId, LocalDate weekStart, LocalDate weekEnd) {
        WeeklyReportDraft draft = WeeklyReportDraft.createPending(
                createTitle(weekStart, weekEnd),
                weekStart,
                weekEnd,
                aiWriter.modelName(),
                WeeklyReportPromptFactory.PROMPT_VERSION,
                accountId
        );
        WeeklyReportDraft savedDraft = weeklyReportDraftRepository.save(draft);
        Long draftId = savedDraft.getIdOrThrow();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                asyncGenerationService.generateDraft(draftId);
            }
        });
        return toDetail(savedDraft, null);
    }

    private String createTitle(LocalDate weekStart, LocalDate weekEnd) {
        return "주간 운영 보고서 (%s ~ %s)".formatted(weekStart, weekEnd);
    }

    private WeeklyReportDraftDetail toDetail(WeeklyReportDraft draft, @Nullable WeeklyReportSnapshot snapshot) {
        return new WeeklyReportDraftDetail(
                draft.getIdOrThrow(),
                draft.getTitle(),
                draft.getWeekStart(),
                draft.getWeekEnd(),
                draft.getStatus(),
                draft.getReportMarkdown(),
                snapshot != null ? toSnapshotSummary(snapshot) : emptySnapshotSummary(),
                draft.getSnapshotJson(),
                draft.getFailureReason(),
                draft.getCreatedAt(),
                draft.getUpdatedAt()
        );
    }

    static WeeklyReportSnapshotSummary toSnapshotSummary(WeeklyReportSnapshot snapshot) {
        return new WeeklyReportSnapshotSummary(
                snapshot.employees().totalEmployees(),
                snapshot.employees().onLeaveEmployees(),
                snapshot.projects().inProgressProjects(),
                snapshot.projects().startedThisWeek(),
                snapshot.projects().endedThisWeek(),
                snapshot.revenue().monthlySummaryAvailable()
        );
    }

    static WeeklyReportSnapshotSummary emptySnapshotSummary() {
        return new WeeklyReportSnapshotSummary(0, 0, 0, 0, 0, false);
    }
}
