package kr.co.abacus.abms.application.weeklyreport.outbound;

import java.util.List;
import java.util.Optional;

import kr.co.abacus.abms.domain.weeklyreport.WeeklyReportDraft;

public interface WeeklyReportDraftRepository {

    WeeklyReportDraft save(WeeklyReportDraft draft);

    Optional<WeeklyReportDraft> findById(Long id);

    Optional<WeeklyReportDraft> findByIdAndCreatedByAccountIdAndDeletedFalse(Long id, Long accountId);

    List<WeeklyReportDraft> findAllByCreatedByAccountIdAndDeletedFalseOrderByCreatedAtDesc(Long accountId);
}
