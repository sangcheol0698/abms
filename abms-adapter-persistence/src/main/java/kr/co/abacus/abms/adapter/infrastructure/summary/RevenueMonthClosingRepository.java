package kr.co.abacus.abms.adapter.infrastructure.summary;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.abacus.abms.domain.summary.RevenueMonthClosing;

public interface RevenueMonthClosingRepository
        extends JpaRepository<RevenueMonthClosing, Long>,
        kr.co.abacus.abms.application.summary.outbound.RevenueMonthClosingRepository {
}
