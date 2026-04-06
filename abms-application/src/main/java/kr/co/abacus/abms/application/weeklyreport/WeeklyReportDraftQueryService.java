package kr.co.abacus.abms.application.weeklyreport;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportDraftDetail;
import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportDraftListItem;
import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportSnapshot;
import kr.co.abacus.abms.application.weeklyreport.inbound.WeeklyReportDraftFinder;
import kr.co.abacus.abms.application.weeklyreport.outbound.WeeklyReportDraftRepository;
import kr.co.abacus.abms.domain.weeklyreport.WeeklyReportDraft;

@Service
@Transactional(readOnly = true)
public class WeeklyReportDraftQueryService implements WeeklyReportDraftFinder {

    private final WeeklyReportDraftRepository weeklyReportDraftRepository;
    private final WeeklyReportSnapshotJsonMapper snapshotJsonMapper;

    public WeeklyReportDraftQueryService(
            WeeklyReportDraftRepository weeklyReportDraftRepository,
            WeeklyReportSnapshotJsonMapper snapshotJsonMapper
    ) {
        this.weeklyReportDraftRepository = weeklyReportDraftRepository;
        this.snapshotJsonMapper = snapshotJsonMapper;
    }

    @Override
    public List<WeeklyReportDraftListItem> findRecentDrafts(Long accountId) {
        return weeklyReportDraftRepository.findAllByCreatedByAccountIdAndDeletedFalseOrderByCreatedAtDesc(accountId)
                .stream()
                .map(this::toListItem)
                .toList();
    }

    @Override
    public WeeklyReportDraftDetail findDraftDetail(Long accountId, Long draftId) {
        WeeklyReportDraft draft = weeklyReportDraftRepository.findByIdAndCreatedByAccountIdAndDeletedFalse(draftId, accountId)
                .orElseThrow(() -> new IllegalArgumentException("주간 보고서 초안을 찾을 수 없습니다: " + draftId));
        WeeklyReportSnapshot snapshot = parseSnapshot(draft);
        return new WeeklyReportDraftDetail(
                draft.getIdOrThrow(),
                draft.getTitle(),
                draft.getWeekStart(),
                draft.getWeekEnd(),
                draft.getStatus(),
                draft.getReportMarkdown(),
                snapshot != null ? WeeklyReportDraftCommandService.toSnapshotSummary(snapshot)
                        : WeeklyReportDraftCommandService.emptySnapshotSummary(),
                draft.getSnapshotJson(),
                draft.getFailureReason(),
                draft.getCreatedAt(),
                draft.getUpdatedAt()
        );
    }

    private WeeklyReportDraftListItem toListItem(WeeklyReportDraft draft) {
        WeeklyReportSnapshot snapshot = parseSnapshot(draft);
        return new WeeklyReportDraftListItem(
                draft.getIdOrThrow(),
                draft.getTitle(),
                draft.getWeekStart(),
                draft.getWeekEnd(),
                draft.getStatus(),
                draft.getReportMarkdown(),
                snapshot != null ? WeeklyReportDraftCommandService.toSnapshotSummary(snapshot)
                        : WeeklyReportDraftCommandService.emptySnapshotSummary(),
                draft.getFailureReason(),
                draft.getCreatedAt(),
                draft.getUpdatedAt()
        );
    }

    private @Nullable WeeklyReportSnapshot parseSnapshot(WeeklyReportDraft draft) {
        String snapshotJson = draft.getSnapshotJson();
        if (snapshotJson == null || snapshotJson.isBlank()) {
            return null;
        }
        return snapshotJsonMapper.fromJson(snapshotJson);
    }
}
