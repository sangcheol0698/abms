package kr.co.abacus.abms.summary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import kr.co.abacus.abms.common.domain.BaseEntity;
import kr.co.abacus.abms.common.domain.Money;

/**
 * 전사 월 정직원 비용 집계. 프로젝트에 배분되지 않은 정직원 비용(유휴 비용)을 추적한다.
 */
@Entity
@Table(name = "tb_company_monthly_cost_summary")
public class CompanyMonthlyCostSummary extends BaseEntity {

    @Column(nullable = false, unique = true)
    private LocalDate targetMonth;

    @Column(nullable = false)
    private LocalDateTime calculatedAt;

    @Column(nullable = false)
    private Money totalFullTimeCost;

    @Column(nullable = false)
    private Money allocatedFullTimeCost;

    @Column(nullable = false)
    private Money unallocatedFullTimeCost;

    protected CompanyMonthlyCostSummary() {
    }

    public static CompanyMonthlyCostSummary create(YearMonth month, Money total, Money allocated) {
        CompanyMonthlyCostSummary summary = new CompanyMonthlyCostSummary();
        summary.targetMonth = month.atDay(1);
        summary.update(total, allocated);
        return summary;
    }

    public void update(Money total, Money allocated) {
        this.totalFullTimeCost = total;
        this.allocatedFullTimeCost = allocated;
        this.unallocatedFullTimeCost = total.minus(allocated);
        this.calculatedAt = LocalDateTime.now();
    }

    public LocalDate getTargetMonth() {
        return targetMonth;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public Money getTotalFullTimeCost() {
        return totalFullTimeCost;
    }

    public Money getAllocatedFullTimeCost() {
        return allocatedFullTimeCost;
    }

    public Money getUnallocatedFullTimeCost() {
        return unallocatedFullTimeCost;
    }

}
