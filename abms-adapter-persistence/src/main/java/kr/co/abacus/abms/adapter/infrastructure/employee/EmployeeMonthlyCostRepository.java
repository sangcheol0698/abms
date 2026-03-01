package kr.co.abacus.abms.adapter.infrastructure.employee;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.abacus.abms.domain.employee.EmployeeMonthlyCost;

public interface EmployeeMonthlyCostRepository
        extends JpaRepository<EmployeeMonthlyCost, Long>,
        kr.co.abacus.abms.application.employee.outbound.EmployeeMonthlyCostRepository {
}
