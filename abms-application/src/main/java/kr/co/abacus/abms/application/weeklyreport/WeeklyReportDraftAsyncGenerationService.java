package kr.co.abacus.abms.application.weeklyreport;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jakarta.annotation.PreDestroy;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import kr.co.abacus.abms.application.weeklyreport.outbound.WeeklyReportDraftRepository;
import kr.co.abacus.abms.domain.weeklyreport.WeeklyReportDraft;
import kr.co.abacus.abms.domain.weeklyreport.WeeklyReportStatus;

@Service
class WeeklyReportDraftAsyncGenerationService {

    private final WeeklyReportSnapshotService snapshotService;
    private final WeeklyReportInsightService insightService;
    private final WeeklyReportAiWriter aiWriter;
    private final WeeklyReportSnapshotJsonMapper snapshotJsonMapper;
    private final WeeklyReportDraftRepository weeklyReportDraftRepository;
    private final TransactionTemplate transactionTemplate;
    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    private final Map<Long, Future<?>> runningTasks = new ConcurrentHashMap<>();

    WeeklyReportDraftAsyncGenerationService(
            WeeklyReportSnapshotService snapshotService,
            WeeklyReportInsightService insightService,
            WeeklyReportAiWriter aiWriter,
            WeeklyReportSnapshotJsonMapper snapshotJsonMapper,
            WeeklyReportDraftRepository weeklyReportDraftRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.snapshotService = snapshotService;
        this.insightService = insightService;
        this.aiWriter = aiWriter;
        this.snapshotJsonMapper = snapshotJsonMapper;
        this.weeklyReportDraftRepository = weeklyReportDraftRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public void generateDraft(Long draftId) {
        Future<?> future = executorService.submit(() -> runGeneration(draftId));
        runningTasks.put(draftId, future);
    }

    public void cancelDraft(Long draftId) {
        Future<?> future = runningTasks.remove(draftId);
        if (future != null) {
            future.cancel(true);
        }
    }

    private void runGeneration(Long draftId) {
        try {
            WeeklyReportDraft draftForSnapshot = updateStatus(draftId, WeeklyReportDraft::markCollecting);
            if (shouldStop(draftForSnapshot)) {
                return;
            }
            var snapshot = snapshotService.createSnapshot(draftForSnapshot.getWeekStart(), draftForSnapshot.getWeekEnd());

            WeeklyReportDraft draftForGeneration = updateStatus(draftId, WeeklyReportDraft::markGenerating);
            if (shouldStop(draftForGeneration)) {
                return;
            }
            var insightData = insightService.createInsights(snapshot);
            String reportMarkdown = aiWriter.write(snapshot, insightData);
            String snapshotJson = snapshotJsonMapper.toJson(snapshot);

            transactionTemplate.executeWithoutResult(status -> {
                WeeklyReportDraft draft = getDraftOrThrow(draftId);
                if (draft.getStatus() == WeeklyReportStatus.CANCELLED || Thread.currentThread().isInterrupted()) {
                    return;
                }
                draft.complete(reportMarkdown, snapshotJson);
                weeklyReportDraftRepository.save(draft);
            });
        } catch (RuntimeException exception) {
            transactionTemplate.executeWithoutResult(status -> {
                WeeklyReportDraft draft = getDraftOrThrow(draftId);
                if (draft.getStatus() == WeeklyReportStatus.CANCELLED || Thread.currentThread().isInterrupted()) {
                    return;
                }
                String failureReason = exception.getMessage() != null && !exception.getMessage().isBlank()
                        ? exception.getMessage()
                        : "주간 운영 보고서 생성 중 오류가 발생했습니다.";
                draft.fail(truncateFailureReason(failureReason));
                weeklyReportDraftRepository.save(draft);
            });
        } finally {
            runningTasks.remove(draftId);
        }
    }

    private boolean shouldStop(WeeklyReportDraft draft) {
        return draft.getStatus() == WeeklyReportStatus.CANCELLED || Thread.currentThread().isInterrupted();
    }

    private WeeklyReportDraft updateStatus(Long draftId, java.util.function.Consumer<WeeklyReportDraft> updater) {
        final WeeklyReportDraft[] holder = new WeeklyReportDraft[1];
        transactionTemplate.executeWithoutResult(status -> {
            WeeklyReportDraft draft = getDraftOrThrow(draftId);
            updater.accept(draft);
            holder[0] = weeklyReportDraftRepository.save(draft);
        });
        return holder[0];
    }

    private WeeklyReportDraft getDraftOrThrow(Long draftId) {
        return weeklyReportDraftRepository.findById(draftId)
                .orElseThrow(() -> new IllegalArgumentException("주간 보고서 초안을 찾을 수 없습니다: " + draftId));
    }

    private String truncateFailureReason(String failureReason) {
        if (failureReason.length() <= 500) {
            return failureReason;
        }
        return failureReason.substring(0, 500);
    }

    @PreDestroy
    void shutdown() {
        executorService.shutdownNow();
    }
}
