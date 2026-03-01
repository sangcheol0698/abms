package kr.co.abacus.abms.application.employee.outbound;

import java.util.List;
import java.util.Optional;

import kr.co.abacus.abms.domain.employee.EmployeeMonthlyCost;

public interface EmployeeMonthlyCostRepository {

    Optional<EmployeeMonthlyCost> findByEmployeeIdAndCostMonth(Long employeeId, String costMonth);

    <S extends EmployeeMonthlyCost> List<S> saveAll(Iterable<S> employeeMonthlyCosts);

}
