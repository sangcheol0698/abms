package kr.co.abacus.abms.adapter.infrastructure.summary;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;

public interface MonthlyRevenueSummaryRepository
        extends JpaRepository<MonthlyRevenueSummary, Long>,
        kr.co.abacus.abms.application.summary.outbound.MonthlyRevenueSummaryRepository {

    @Override
    @Modifying
    @Query("DELETE FROM MonthlyRevenueSummary s WHERE s.summaryDate >= :start AND s.summaryDate <= :end")
    void deleteBySummaryDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
