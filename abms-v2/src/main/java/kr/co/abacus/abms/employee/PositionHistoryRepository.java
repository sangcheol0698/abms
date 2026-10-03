package kr.co.abacus.abms.employee;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PositionHistoryRepository extends JpaRepository<PositionHistory, Long> {

    List<PositionHistory> findAllByEmployeeIdOrderByPeriodStartDateDesc(Long employeeId);

    @Query("select h from PositionHistory h where h.employeeId = :employeeId and h.period.endDate is null")
    Optional<PositionHistory> findOpen(Long employeeId);

}
