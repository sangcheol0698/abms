package kr.co.abacus.abms.application.summary.outbound;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;

public interface MonthlyRevenueSummaryRepository extends JpaRepository<MonthlyRevenueSummary, Long> {

    Optional<MonthlyRevenueSummary> findFirstBySummaryDateBetweenOrderBySummaryDateDesc(LocalDate start, LocalDate end);
}
