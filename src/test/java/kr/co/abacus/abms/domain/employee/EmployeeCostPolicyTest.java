package kr.co.abacus.abms.domain.employee;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kr.co.abacus.abms.domain.shared.Money;

class EmployeeCostPolicyTest {

    @Test
    @DisplayName("비용 금액 계산: 기본급에 비용 정책이 적용된 금액이 반환된다")
    void calculateCost_Success() {
        // given
        double overheadRate = 0.1;
        double sgaRate = 0.05;

        EmployeeCostPolicy policy = EmployeeCostPolicy.create(
            2024,
            EmployeeType.FULL_TIME,
            overheadRate,
            sgaRate
        );

        Money annualSalary = new Money(new BigDecimal("36000000"));

        // when
        Money resultOverHeadAmount = policy.calculateOverheadCost(annualSalary);
        Money resultSgaAmount = policy.calculateSgaCost(annualSalary);

        // then
        BigDecimal expectedOverHeadAmount = new BigDecimal("300000");
        BigDecimal expectedSgaAmount = new BigDecimal("150000");

        assertThat(resultOverHeadAmount.amount()).isEqualByComparingTo(expectedOverHeadAmount);
        assertThat(resultSgaAmount.amount()).isEqualByComparingTo(expectedSgaAmount);
    }


}