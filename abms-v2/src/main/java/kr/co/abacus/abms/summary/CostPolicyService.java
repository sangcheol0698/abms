package kr.co.abacus.abms.summary;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.NotFoundException;
import kr.co.abacus.abms.employee.EmployeeType;

@Service
@Transactional
public class CostPolicyService {

    private final EmployeeCostPolicyRepository policyRepository;

    public CostPolicyService(EmployeeCostPolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @Transactional(readOnly = true)
    public List<EmployeeCostPolicy> all() {
        return policyRepository.findAllByOrderByApplyYearDescTypeAsc();
    }

    public EmployeeCostPolicy create(int year, EmployeeType type, BigDecimal overheadRate, BigDecimal sgaRate) {
        if (policyRepository.findByApplyYearAndType(year, type).isPresent()) {
            throw new BusinessException(year + "년 " + type.label() + " 정책이 이미 있습니다.");
        }
        return policyRepository.save(EmployeeCostPolicy.create(year, type, overheadRate, sgaRate));
    }

    public void update(Long id, BigDecimal overheadRate, BigDecimal sgaRate) {
        policyRepository.findById(id).orElseThrow(() -> NotFoundException.of("원가 정책", id))
                .changeRates(overheadRate, sgaRate);
    }

    public void delete(Long id) {
        policyRepository.delete(policyRepository.findById(id).orElseThrow(() -> NotFoundException.of("원가 정책", id)));
    }

}
