package kr.co.abacus.abms.summary;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import kr.co.abacus.abms.common.domain.BaseEntity;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.employee.EmployeeType;

/**
 * 연도·고용유형별 원가 정책.
 * <pre>월 원가 = 월 기본급 × (1 + 제경비율 + 판관비율)</pre>
 */
@Entity
@Table(name = "tb_employee_cost_policy")
public class EmployeeCostPolicy extends BaseEntity {

    @Column(nullable = false)
    private int applyYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type", nullable = false, length = 20)
    private EmployeeType type;

    @Column(nullable = false, precision = 6, scale = 4)
    private BigDecimal overheadRate;

    @Column(nullable = false, precision = 6, scale = 4)
    private BigDecimal sgaRate;

    protected EmployeeCostPolicy() {
    }

    public static EmployeeCostPolicy create(int applyYear, EmployeeType type, BigDecimal overheadRate, BigDecimal sgaRate) {
        EmployeeCostPolicy policy = new EmployeeCostPolicy();
        policy.applyYear = applyYear;
        policy.type = Objects.requireNonNull(type);
        policy.changeRates(overheadRate, sgaRate);
        return policy;
    }

    public void changeRates(BigDecimal overheadRate, BigDecimal sgaRate) {
        if (overheadRate.signum() < 0 || sgaRate.signum() < 0) {
            throw new BusinessException("비율은 음수일 수 없습니다.");
        }
        if (overheadRate.compareTo(BigDecimal.ONE) > 0 || sgaRate.compareTo(BigDecimal.ONE) > 0) {
            throw new BusinessException("비율은 1(100%)을 넘을 수 없습니다.");
        }
        this.overheadRate = overheadRate;
        this.sgaRate = sgaRate;
    }

    public CostBreakdown breakdown(Money monthlySalary) {
        Money overhead = monthlySalary.times(overheadRate);
        Money sga = monthlySalary.times(sgaRate);
        return new CostBreakdown(monthlySalary, overhead, sga, monthlySalary.plus(overhead).plus(sga));
    }

    public int getApplyYear() {
        return applyYear;
    }

    public EmployeeType getType() {
        return type;
    }

    public BigDecimal getOverheadRate() {
        return overheadRate;
    }

    public BigDecimal getSgaRate() {
        return sgaRate;
    }

    public record CostBreakdown(Money monthlySalary, Money overheadCost, Money sgaCost, Money totalCost) {
    }

}
