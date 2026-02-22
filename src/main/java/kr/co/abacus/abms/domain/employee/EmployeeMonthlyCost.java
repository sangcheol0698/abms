package kr.co.abacus.abms.domain.employee;

import java.time.LocalDate;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import kr.co.abacus.abms.domain.AbstractEntity;
import kr.co.abacus.abms.domain.shared.Money;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_employee_monthly_cost",
        comment = "월별 인력 비용 (월급 + 제경비 + 판관비)",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"employee_id", "cost_date"}) // 직원+비용발생월 중복 방지
        })
public class EmployeeMonthlyCost extends AbstractEntity {

    @Column(name = "employee_id", nullable = false, comment = "직원ID")
    private Long employeeId;

    @Column(name = "cost_date", nullable = false, length = 6, comment = "비용발생월 (202602)")
    private String costDate;

    @Embedded
    @AttributeOverride(name = "amount",
        column = @Column(name = "monthly_salary", nullable = false, comment = "월 기본급"))
    private Money monthlySalary;

    @Embedded
    @AttributeOverride(name = "amount",
        column = @Column(name = "overhead_cost", nullable = false, comment = "월 제경비 (월 기본급 * 제경비율)"))
    private Money overHeadCost;

    @Embedded
    @AttributeOverride(name = "amount",
        column = @Column(name = "sga_cost", nullable = false, comment = "월 판관비 (월 기본급 * 판관비율)"))
    private Money sgaCost;

    @Embedded
    @AttributeOverride(name = "amount",
        column = @Column(name = "total_cost", nullable = false, comment = "월 총 비용 (월 기본급 + 월 제경비 + 월 판관비)"))
    private Money totalCost;

    @Builder(access = AccessLevel.PRIVATE)
    private EmployeeMonthlyCost(Long employeeId, String costDate, Money monthlySalary, Money overHeadCost, Money sgaCost, Money totalCost) {
        this.employeeId = employeeId;
        this.costDate = costDate;
        this.monthlySalary = monthlySalary;
        this.overHeadCost = overHeadCost;
        this.sgaCost = sgaCost;
        this.totalCost = totalCost;
    }

    public static EmployeeMonthlyCost create(Long employeeId, String costDate, Money monthlySalary, Money overHeadCost, Money sgaCost, Money totalCost) {
        return EmployeeMonthlyCost.builder()
            .employeeId(employeeId)
            .costDate(costDate)
            .monthlySalary(monthlySalary)
            .overHeadCost(overHeadCost)
            .sgaCost(sgaCost)
            .totalCost(totalCost)
            .build();
    }
}
