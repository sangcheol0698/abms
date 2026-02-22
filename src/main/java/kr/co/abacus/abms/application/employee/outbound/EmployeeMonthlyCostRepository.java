package kr.co.abacus.abms.application.employee.outbound;

import static kr.co.abacus.abms.domain.employee.QEmployee.*;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.abacus.abms.domain.employee.EmployeeMonthlyCost;

public interface EmployeeMonthlyCostRepository extends JpaRepository<EmployeeMonthlyCost, Long> {

    Optional<EmployeeMonthlyCost> findByEmployeeIdAndCostMonth(Long employeeId, String costMonth);

}
