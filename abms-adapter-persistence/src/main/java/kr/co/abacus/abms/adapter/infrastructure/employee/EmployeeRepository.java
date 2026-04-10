package kr.co.abacus.abms.adapter.infrastructure.employee;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.employee.Employee;

public interface EmployeeRepository
        extends Repository<Employee, Long>,
        kr.co.abacus.abms.application.employee.outbound.EmployeeRepository {

    @Override
    @Query("""
            select count(e)
            from Employee e
            where e.deleted = false
              and e.joinDate <= :targetDate
              and (e.resignationDate is null or e.resignationDate > :targetDate)
            """)
    int countEmployedAsOf(@Param("targetDate") LocalDate targetDate);

    @Override
    @Query("""
            select count(e)
            from Employee e
            where e.deleted = false
              and e.type = :type
              and e.joinDate <= :targetDate
              and (e.resignationDate is null or e.resignationDate > :targetDate)
            """)
    int countEmployedAsOfByType(@Param("targetDate") LocalDate targetDate, @Param("type") EmployeeType type);
}
