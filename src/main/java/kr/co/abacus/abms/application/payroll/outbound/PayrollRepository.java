package kr.co.abacus.abms.application.payroll.outbound;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

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

}
