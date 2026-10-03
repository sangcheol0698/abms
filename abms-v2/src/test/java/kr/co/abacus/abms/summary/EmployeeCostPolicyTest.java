package kr.co.abacus.abms.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.employee.EmployeeType;
import kr.co.abacus.abms.summary.EmployeeCostPolicy.CostBreakdown;

class EmployeeCostPolicyTest {

    @Test
    void 월_원가는_월급에_제경비와_판관비를_더한_값이다() {
        EmployeeCostPolicy policy = EmployeeCostPolicy.create(2026, EmployeeType.FULL_TIME, new BigDecimal("0.10"), new BigDecimal("0.05"));

        CostBreakdown cost = policy.breakdown(Money.wons(10_000_000));

        assertThat(cost.overheadCost()).isEqualTo(Money.wons(1_000_000));
        assertThat(cost.sgaCost()).isEqualTo(Money.wons(500_000));
        assertThat(cost.totalCost()).isEqualTo(Money.wons(11_500_000));
    }

    @Test
    void 비율은_0에서_1_사이여야_한다() {
        assertThatThrownBy(() -> EmployeeCostPolicy.create(2026, EmployeeType.FULL_TIME, new BigDecimal("-0.1"), BigDecimal.ZERO))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> EmployeeCostPolicy.create(2026, EmployeeType.FULL_TIME, new BigDecimal("1.5"), BigDecimal.ZERO))
                .isInstanceOf(BusinessException.class);
    }

}
