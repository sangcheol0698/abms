package kr.co.abacus.abms.application.salaryhistory.required;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.salaryhistory.SalaryHistory;

public interface SalaryHistoryRepository extends Repository<SalaryHistory, UUID> {

    SalaryHistory save(SalaryHistory salaryHistory);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        "SELECT sh " +
        "FROM SalaryHistory sh " +
        "WHERE sh.employeeId = :employeeId " +
            "AND sh.period.endDate IS NULL " +
            "AND sh.deleted = false"
    )
    Optional<SalaryHistory> findCurrentSalaryByEmployeeId(UUID employeeId);

}
