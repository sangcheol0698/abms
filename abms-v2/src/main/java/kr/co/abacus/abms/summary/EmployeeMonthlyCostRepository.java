package kr.co.abacus.abms.summary;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeMonthlyCostRepository extends JpaRepository<EmployeeMonthlyCost, Long> {

    List<EmployeeMonthlyCost> findAllByTargetMonth(LocalDate targetMonth);

    Optional<EmployeeMonthlyCost> findByEmployeeIdAndTargetMonth(Long employeeId, LocalDate targetMonth);

}
