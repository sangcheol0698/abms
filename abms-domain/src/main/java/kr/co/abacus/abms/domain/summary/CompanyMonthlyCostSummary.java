package kr.co.abacus.abms.domain.summary;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import kr.co.abacus.abms.domain.AbstractEntity;
import kr.co.abacus.abms.domain.shared.Money;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "tb_company_monthly_cost_summary",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_COMPANY_MONTHLY_COST_SUMMARY_TARGET_MONTH",
                columnNames = "target_month"
        ),
        indexes = @Index(name = "IDX_COMPANY_MONTHLY_COST_SUMMARY_TARGET_MONTH", columnList = "target_month")
)
public class CompanyMonthlyCostSummary extends AbstractEntity {

    @Column(name = "target_month", nullable = false, comment = "집계대상월")
    private LocalDate targetMonth;

    @Column(name = "calculated_at", nullable = false, comment = "집계계산일시")
    private LocalDateTime calculatedAt;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "total_full_time_employee_cost", nullable = false, comment = "정직원 월 원가 합계"))
    private Money totalFullTimeEmployeeCost;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "allocated_full_time_employee_cost", nullable = false, comment = "정직원 프로젝트 배부 원가"))
    private Money allocatedFullTimeEmployeeCost;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "unallocated_full_time_employee_cost", nullable = false, comment = "정직원 비가용 원가"))
    private Money unallocatedFullTimeEmployeeCost;

    public static CompanyMonthlyCostSummary create(
            LocalDate targetMonth,
            Money totalFullTimeEmployeeCost,
            Money allocatedFullTimeEmployeeCost,
            Money unallocatedFullTimeEmployeeCost
    ) {
        CompanyMonthlyCostSummary summary = new CompanyMonthlyCostSummary();
        summary.targetMonth = normalizeTargetMonth(targetMonth);
        summary.totalFullTimeEmployeeCost = Objects.requireNonNull(totalFullTimeEmployeeCost);
        summary.allocatedFullTimeEmployeeCost = Objects.requireNonNull(allocatedFullTimeEmployeeCost);
        summary.unallocatedFullTimeEmployeeCost = Objects.requireNonNull(unallocatedFullTimeEmployeeCost);
        summary.calculatedAt = LocalDateTime.now();
        return summary;
    }

    public void update(
            Money totalFullTimeEmployeeCost,
            Money allocatedFullTimeEmployeeCost,
            Money unallocatedFullTimeEmployeeCost
    ) {
        this.totalFullTimeEmployeeCost = Objects.requireNonNull(totalFullTimeEmployeeCost);
        this.allocatedFullTimeEmployeeCost = Objects.requireNonNull(allocatedFullTimeEmployeeCost);
        this.unallocatedFullTimeEmployeeCost = Objects.requireNonNull(unallocatedFullTimeEmployeeCost);
        this.calculatedAt = LocalDateTime.now();
    }

    private static LocalDate normalizeTargetMonth(LocalDate targetMonth) {
        return YearMonth.from(Objects.requireNonNull(targetMonth)).atDay(1);
    }
}
