package kr.co.abacus.abms.summary;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RevenueMonthClosingRepository extends JpaRepository<RevenueMonthClosing, Long> {

    Optional<RevenueMonthClosing> findByTargetMonth(LocalDate targetMonth);

    boolean existsByTargetMonthAndClosedTrue(LocalDate targetMonth);

    List<RevenueMonthClosing> findAllByClosedTrueOrderByTargetMonthDesc();

}
