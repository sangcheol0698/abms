package kr.co.abacus.abms.adapter.infrastructure.payroll;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import kr.co.abacus.abms.domain.payroll.Payroll;

public interface PayrollRepository
        extends Repository<Payroll, Long>,
        kr.co.abacus.abms.application.payroll.outbound.PayrollRepository {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sh " +
            "FROM Payroll sh " +
            "WHERE sh.employeeId = :employeeId " +
            "AND sh.period.endDate IS NULL " +
            "AND sh.deleted = false")
    Optional<Payroll> lockCurrentSalaryByEmployeeId(Long employeeId);

    @Override
    @Query("SELECT sh " +
            "FROM Payroll sh " +
            "WHERE sh.employeeId = :employeeId " +
            "AND sh.period.endDate IS NULL " +
            "AND sh.deleted = false")
    Optional<Payroll> findCurrentSalaryByEmployeeId(Long employeeId);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p " +
            "FROM Payroll p " +
            "WHERE p.employeeId = :employeeId " +
            "AND p.deleted = false " +
            "AND p.period.startDate <= :targetDate " +
            "AND (p.period.endDate IS NULL OR :targetDate <= p.period.endDate)")
    Optional<Payroll> lockByEmployeeIdAndTargetDate(@Param("employeeId") Long employeeId,
                                                    @Param("targetDate") LocalDate targetDate);

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p " +
            "FROM Payroll p " +
            "WHERE p.employeeId = :employeeId " +
            "AND p.deleted = false " +
            "AND p.period.startDate > :startDate " +
            "ORDER BY p.period.startDate ASC")
    Optional<Payroll> lockNextSalaryByEmployeeId(@Param("employeeId") Long employeeId,
                                                 @Param("startDate") LocalDate startDate);

    @Override
    @Query("SELECT p " +
            "FROM Payroll p " +
            "WHERE p.employeeId = :employeeId " +
            "AND p.deleted = false " +
            "AND p.period.startDate <= :targetDate " +
            "AND (p.period.endDate IS NULL OR :targetDate <= p.period.endDate)")
    Optional<Payroll> findByEmployeeIdAndTargetDate(@Param("employeeId") Long employeeId,
                                                    @Param("targetDate") LocalDate targetDate);

    @Override
    @Query("SELECT p " +
            "FROM Payroll p " +
            "WHERE p.employeeId = :employeeId " +
            "AND p.deleted = false " +
            "ORDER BY p.period.startDate DESC")
    List<Payroll> findAllByEmployeeId(@Param("employeeId") Long employeeId);
}
