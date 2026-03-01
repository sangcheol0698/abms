package kr.co.abacus.abms.adapter.infrastructure.summary;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;

public interface MonthlyRevenueSummaryRepository
        extends JpaRepository<MonthlyRevenueSummary, Long>,
        kr.co.abacus.abms.application.summary.outbound.MonthlyRevenueSummaryRepository {
}
