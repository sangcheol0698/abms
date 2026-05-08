package kr.co.abacus.abms.application.summary.outbound;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import kr.co.abacus.abms.domain.summary.CompanyMonthlyCostSummary;

public interface CompanyMonthlyCostSummaryRepository {

    Optional<CompanyMonthlyCostSummary> findByTargetMonthAndDeletedFalse(LocalDate targetMonth);

    List<CompanyMonthlyCostSummary> findAllByTargetMonthBetweenAndDeletedFalseOrderByTargetMonthAsc(
            LocalDate start,
            LocalDate end
    );

    <S extends CompanyMonthlyCostSummary> List<S> saveAll(Iterable<S> summaries);
}
