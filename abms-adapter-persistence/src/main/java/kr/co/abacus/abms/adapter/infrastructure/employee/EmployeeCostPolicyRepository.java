package kr.co.abacus.abms.adapter.infrastructure.employee;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.employee.EmployeeCostPolicy;

public interface EmployeeCostPolicyRepository
        extends Repository<EmployeeCostPolicy, Long>,
        kr.co.abacus.abms.application.employee.outbound.EmployeeCostPolicyRepository {
}
