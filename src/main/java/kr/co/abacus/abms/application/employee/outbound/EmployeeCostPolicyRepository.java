package kr.co.abacus.abms.application.employee.outbound;

import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.employee.EmployeeCostPolicy;

public interface EmployeeCostPolicyRepository extends Repository<EmployeeCostPolicy, Long> {

    EmployeeCostPolicy save(EmployeeCostPolicy employeeCostPolicy);

    EmployeeCostPolicy findByApplyYearAndType(Integer applyYear, String type);

}
