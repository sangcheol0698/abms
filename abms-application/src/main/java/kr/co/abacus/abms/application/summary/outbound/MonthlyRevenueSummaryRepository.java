package kr.co.abacus.abms.application.summary.outbound;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;

public interface MonthlyRevenueSummaryRepository {

    Optional<MonthlyRevenueSummary> findByProjectIdAndTargetMonthAndDeletedFalse(Long projectId, LocalDate targetMonth);

    List<MonthlyRevenueSummary> findAllByTargetMonthAndDeletedFalseOrderByProjectIdAsc(LocalDate targetMonth);

    List<MonthlyRevenueSummary> findAllByLeadDepartmentIdAndTargetMonthBetweenAndDeletedFalseOrderByTargetMonthAscProjectIdAsc(
        Long leadDepartmentId,
        LocalDate start,
        LocalDate end
    );

    List<MonthlyRevenueSummary> findAllByTargetMonthBetweenAndDeletedFalseOrderByTargetMonthAscProjectIdAsc(
        LocalDate start,
        LocalDate end
    );

    <S extends MonthlyRevenueSummary> List<S> saveAll(Iterable<S> summaries);

    void delete(MonthlyRevenueSummary summary);

}
