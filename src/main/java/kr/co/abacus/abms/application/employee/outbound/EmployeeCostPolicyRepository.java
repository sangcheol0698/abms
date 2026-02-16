package kr.co.abacus.abms.application.employee.outbound;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.employee.EmployeeCostPolicy;
import kr.co.abacus.abms.domain.employee.EmployeeType;

public interface EmployeeCostPolicyRepository extends Repository<EmployeeCostPolicy, Long> {

    EmployeeCostPolicy save(EmployeeCostPolicy employeeCostPolicy);

    Optional<EmployeeCostPolicy> findByApplyYearAndType(Integer applyYear, EmployeeType type);

}
