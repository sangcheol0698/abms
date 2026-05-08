package kr.co.abacus.abms.adapter.infrastructure.summary;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.abacus.abms.domain.summary.CompanyMonthlyCostSummary;

public interface CompanyMonthlyCostSummaryRepository
        extends JpaRepository<CompanyMonthlyCostSummary, Long>,
        kr.co.abacus.abms.application.summary.outbound.CompanyMonthlyCostSummaryRepository {
}
