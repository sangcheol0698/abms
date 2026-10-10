package kr.co.abacus.abms.summary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.ClosedMonthGuard;
import kr.co.abacus.abms.common.domain.NotFoundException;
import kr.co.abacus.abms.employee.EmployeeType;

@Service
@Transactional
public class CostPolicyService {

    private final EmployeeCostPolicyRepository policyRepository;
    private final ClosedMonthGuard closedMonthGuard;

    public CostPolicyService(EmployeeCostPolicyRepository policyRepository, ClosedMonthGuard closedMonthGuard) {
        this.policyRepository = policyRepository;
        this.closedMonthGuard = closedMonthGuard;
    }

    @Transactional(readOnly = true)
    public List<EmployeeCostPolicy> all() {
        return policyRepository.findAllByOrderByApplyYearDescTypeAsc();
    }

    public EmployeeCostPolicy create(int year, EmployeeType type, BigDecimal overheadRate, BigDecimal sgaRate) {
        if (policyRepository.findByApplyYearAndType(year, type).isPresent()) {
            throw new BusinessException(year + "년 " + type.label() + " 정책이 이미 있습니다.");
        }
        checkEffectiveYearsOpen(year, type, "원가 정책 등록");
        return policyRepository.save(EmployeeCostPolicy.create(year, type, overheadRate, sgaRate));
    }

    public void update(Long id, BigDecimal overheadRate, BigDecimal sgaRate) {
        EmployeeCostPolicy policy = policyRepository.findById(id).orElseThrow(() -> NotFoundException.of("원가 정책", id));
        checkEffectiveYearsOpen(policy.getApplyYear(), policy.getType(), "원가 정책 수정");
        policy.changeRates(overheadRate, sgaRate);
    }

    public void delete(Long id) {
        EmployeeCostPolicy policy = policyRepository.findById(id).orElseThrow(() -> NotFoundException.of("원가 정책", id));
        checkEffectiveYearsOpen(policy.getApplyYear(), policy.getType(), "원가 정책 삭제");
        policyRepository.delete(policy);
    }

    /** 정책은 적용 연도부터 다음 정책 연도 전까지 쓰인다. 그 사이에 마감된 월이 있으면 막는다. */
    private void checkEffectiveYearsOpen(int year, EmployeeType type, String subject) {
        LocalDate to = policyRepository.findFirstByTypeAndApplyYearGreaterThanOrderByApplyYearAsc(type, year)
                .map(next -> LocalDate.of(next.getApplyYear() - 1, 12, 1))
                .orElse(null);
        closedMonthGuard.checkOpen(LocalDate.of(year, 1, 1), to, subject);
    }

}
