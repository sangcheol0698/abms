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
 * 프로젝트별 월 손익 집계. (프로젝트, 월) 당 한 행이며 재집계 시 갱신된다.
 * 프로젝트명/주관 부서는 집계 시점의 값을 스냅샷으로 보관한다.
 */
@Entity
@Table(name = "tb_monthly_revenue_summary")
public class MonthlyRevenueSummary extends BaseEntity {

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private String projectCode;

    @Column(nullable = false)
    private String projectName;

    @Column(nullable = false)
    private Long leadDepartmentId;

    @Column(nullable = false)
    private String leadDepartmentName;

    @Column(nullable = false)
    private LocalDate targetMonth;

    @Column(nullable = false)
    private LocalDateTime calculatedAt;

    @Column(nullable = false)
    private Money revenueAmount;

    @Column(nullable = false)
    private Money costAmount;

    @Column(nullable = false)
    private Money profitAmount;

    protected MonthlyRevenueSummary() {
    }

    public static MonthlyRevenueSummary create(Long projectId, YearMonth month, Snapshot snapshot) {
        MonthlyRevenueSummary summary = new MonthlyRevenueSummary();
        summary.projectId = projectId;
        summary.targetMonth = month.atDay(1);
        summary.update(snapshot);
        return summary;
    }

    public void update(Snapshot s) {
        this.projectCode = s.projectCode();
        this.projectName = s.projectName();
        this.leadDepartmentId = s.leadDepartmentId();
        this.leadDepartmentName = s.leadDepartmentName();
        this.revenueAmount = s.revenue();
        this.costAmount = s.cost();
        this.profitAmount = s.revenue().minus(s.cost());
        this.calculatedAt = LocalDateTime.now();
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public String getProjectName() {
        return projectName;
    }

    public Long getLeadDepartmentId() {
        return leadDepartmentId;
    }

    public String getLeadDepartmentName() {
        return leadDepartmentName;
    }

    public LocalDate getTargetMonth() {
        return targetMonth;
    }

    public LocalDateTime getCalculatedAt() {
        return calculatedAt;
    }

    public Money getRevenueAmount() {
        return revenueAmount;
    }

    public Money getCostAmount() {
        return costAmount;
    }

    public Money getProfitAmount() {
        return profitAmount;
    }

    public record Snapshot(
            String projectCode,
            String projectName,
            Long leadDepartmentId,
            String leadDepartmentName,
            Money revenue,
            Money cost
    ) {
    }

}
