package kr.co.abacus.abms.summary;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kr.co.abacus.abms.employee.EmployeeType;

public interface EmployeeCostPolicyRepository extends JpaRepository<EmployeeCostPolicy, Long> {

    List<EmployeeCostPolicy> findAllByOrderByApplyYearDescTypeAsc();

    Optional<EmployeeCostPolicy> findByApplyYearAndType(int applyYear, EmployeeType type);

    /** 해당 연도 정책이 없으면 가장 최근 연도의 정책을 쓴다. */
    Optional<EmployeeCostPolicy> findFirstByTypeAndApplyYearLessThanEqualOrderByApplyYearDesc(EmployeeType type, int applyYear);

}
