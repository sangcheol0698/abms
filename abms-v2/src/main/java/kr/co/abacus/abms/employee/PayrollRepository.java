package kr.co.abacus.abms.employee;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    List<Payroll> findAllByEmployeeIdOrderByPeriodStartDateDesc(Long employeeId);

    @Query("""
            select p from Payroll p
            where p.employeeId = :employeeId
              and p.period.startDate <= :date
              and (p.period.endDate is null or p.period.endDate >= :date)
            order by p.period.startDate desc
            limit 1
            """)
    Optional<Payroll> findEffective(Long employeeId, LocalDate date);

    @Query("select p from Payroll p where p.employeeId = :employeeId and p.period.endDate is null")
    Optional<Payroll> findOpen(Long employeeId);

}
