package kr.co.abacus.abms.summary;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyMonthlyCostSummaryRepository extends JpaRepository<CompanyMonthlyCostSummary, Long> {

    Optional<CompanyMonthlyCostSummary> findByTargetMonth(LocalDate targetMonth);

    List<CompanyMonthlyCostSummary> findAllByTargetMonthBetweenOrderByTargetMonthAsc(LocalDate from, LocalDate to);

}
