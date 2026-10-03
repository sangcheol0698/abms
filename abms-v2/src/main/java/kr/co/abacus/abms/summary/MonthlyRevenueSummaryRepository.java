package kr.co.abacus.abms.summary;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MonthlyRevenueSummaryRepository extends JpaRepository<MonthlyRevenueSummary, Long> {

    List<MonthlyRevenueSummary> findAllByTargetMonthOrderByProjectNameAsc(LocalDate targetMonth);

    List<MonthlyRevenueSummary> findAllByProjectIdOrderByTargetMonthAsc(Long projectId);

    List<MonthlyRevenueSummary> findAllByTargetMonthBetweenOrderByTargetMonthAsc(LocalDate from, LocalDate to);

    List<MonthlyRevenueSummary> findAllByLeadDepartmentIdInAndTargetMonthBetweenOrderByTargetMonthAsc(
            Collection<Long> departmentIds, LocalDate from, LocalDate to);

}
