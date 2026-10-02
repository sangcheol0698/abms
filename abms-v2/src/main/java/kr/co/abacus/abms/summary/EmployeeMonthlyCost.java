package kr.co.abacus.abms.summary;

import java.time.LocalDate;
import java.time.YearMonth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import kr.co.abacus.abms.common.domain.BaseEntity;
import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.summary.EmployeeCostPolicy.CostBreakdown;

/**
 * 직원 월 원가 스냅샷. 재집계 시 같은 (직원, 월) 행을 갱신한다.
 */
@Entity
@Table(name = "tb_employee_monthly_cost")
public class EmployeeMonthlyCost extends BaseEntity {

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private LocalDate targetMonth;

    @Column(nullable = false)
    private Money monthlySalary;

    @Column(nullable = false)
    private Money overheadCost;

    @Column(nullable = false)
    private Money sgaCost;

    @Column(nullable = false)
    private Money totalCost;

    protected EmployeeMonthlyCost() {
    }

    public static EmployeeMonthlyCost create(Long employeeId, YearMonth month, CostBreakdown cost) {
        EmployeeMonthlyCost monthly = new EmployeeMonthlyCost();
        monthly.employeeId = employeeId;
        monthly.targetMonth = month.atDay(1);
        monthly.update(cost);
        return monthly;
    }

    public void update(CostBreakdown cost) {
        this.monthlySalary = cost.monthlySalary();
        this.overheadCost = cost.overheadCost();
        this.sgaCost = cost.sgaCost();
        this.totalCost = cost.totalCost();
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public LocalDate getTargetMonth() {
        return targetMonth;
    }

    public Money getMonthlySalary() {
        return monthlySalary;
    }

    public Money getOverheadCost() {
        return overheadCost;
    }

    public Money getSgaCost() {
        return sgaCost;
    }

    public Money getTotalCost() {
        return totalCost;
    }

}
