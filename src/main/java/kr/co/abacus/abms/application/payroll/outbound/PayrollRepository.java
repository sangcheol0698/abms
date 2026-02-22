package kr.co.abacus.abms.application.payroll.outbound;

import java.time.LocalDate;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import kr.co.abacus.abms.domain.payroll.Payroll;

public interface PayrollRepository extends Repository<Payroll, Long> {

    Payroll save(Payroll payroll);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sh " +
            "FROM Payroll sh " +
            "WHERE sh.employeeId = :employeeId " +
            "AND sh.period.endDate IS NULL " +
            "AND sh.deleted = false")
    Optional<Payroll> findCurrentSalaryByEmployeeId(Long employeeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p " +
        "FROM Payroll p " +
        "WHERE p.employeeId = :employeeId " +
        "AND p.deleted = false " +
        "AND :targetDate BETWEEN p.period.startDate AND p.period.endDate")
    Optional<Payroll> findByEmployeeIdAndTargetDate(@Param("employeeId") Long employeeId,
                                                    @Param("targetDate") LocalDate targetDate);

}
