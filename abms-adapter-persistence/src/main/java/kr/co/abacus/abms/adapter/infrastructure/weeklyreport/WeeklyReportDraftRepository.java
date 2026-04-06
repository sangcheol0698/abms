package kr.co.abacus.abms.adapter.infrastructure.weeklyreport;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.abacus.abms.domain.weeklyreport.WeeklyReportDraft;

public interface WeeklyReportDraftRepository
        extends JpaRepository<WeeklyReportDraft, Long>,
        kr.co.abacus.abms.application.weeklyreport.outbound.WeeklyReportDraftRepository {
}
