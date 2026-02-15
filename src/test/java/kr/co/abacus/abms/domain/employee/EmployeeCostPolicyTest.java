package kr.co.abacus.abms.domain.employee;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kr.co.abacus.abms.domain.shared.Money;

class EmployeeCostPolicyTest {

    @Test
    @DisplayName("인력 비용 계산: 기본급에 제경비와 판관비 요율이 적용된 총 원가가 반환된다")
    void calculateEmployeeCost() {
        // given
        // 1. 테스트 데이터 설정: 제경비 10%, 판관비 5%
        double overheadRate = 0.1;
        double sgaRate = 0.05;

        EmployeeCostPolicy policy = EmployeeCostPolicy.create(
            2026,
            EmployeeType.FULL_TIME,
            overheadRate,
            sgaRate
        );

        // 2. 기본급 설정: 300만 원
        Money baseSalary = new Money(new BigDecimal("3000000"));

        // when
        Money result = policy.calculateEmployeeCost(baseSalary);

        // then
        // 3,000,000 * (1 + 0.1 + 0.05) = 3,450,000
        BigDecimal expectedAmount = new BigDecimal("3450000");

        // Money 내부의 BigDecimal 값을 꺼내서 비교 (소수점 처리 등을 위해 isEqualByComparingTo 권장)
        assertThat(result.amount()).isEqualByComparingTo(expectedAmount);
    }

}