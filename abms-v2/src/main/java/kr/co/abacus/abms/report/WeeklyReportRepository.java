package kr.co.abacus.abms.report;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long> {

    Page<WeeklyReport> findAllByOrderByWeekStartDescIdDesc(Pageable pageable);

}
