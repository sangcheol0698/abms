package kr.co.abacus.abms.domain.employee;

import static java.util.Objects.*;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import kr.co.abacus.abms.domain.shared.Money;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_employee_cost_policy",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"apply_year", "employee_type"}) // 연도+타입 중복 방지
    })
public class EmployeeCostPolicy  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "apply_year", nullable = false, comment = "적용년도")
    private Integer applyYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type", nullable = false, comment = "직원유형")
    private EmployeeType type;

    @Column(name = "overhead_rate", nullable = false, comment = "제경비율")
    private Double overheadRate;

    @Column(name = "sga_rate", nullable = false, comment = "판관비율")
    private Double sgaRate;

    @Builder(access = AccessLevel.PRIVATE)
    private EmployeeCostPolicy(Integer applyYear, EmployeeType type, Double overheadRate, Double sgaRate) {
        this.applyYear = requireNonNull(applyYear);
        this.type = requireNonNull(type);
        this.overheadRate = requireNonNull(overheadRate);
        this.sgaRate = requireNonNull(sgaRate);
    }

    public static EmployeeCostPolicy create(Integer applyYear, EmployeeType type, Double overheadRate, Double sgaRate) {
        return EmployeeCostPolicy.builder()
            .applyYear(applyYear)
            .type(type)
            .overheadRate(overheadRate)
            .sgaRate(sgaRate)
            .build();
    }

    public Money calculateEmployeeCost(Money annualSalary) {
        // 비용 계산 로직 = 월급 * (1 + 제경비 + 판관비)
        annualSalary = annualSalary.divide(BigDecimal.valueOf(12));
        return annualSalary.multiply(BigDecimal.valueOf(1.0 + overheadRate + sgaRate));
    }

    public Money calculateOverheadCost(Money annualSalary) {
        Money monthSalary = annualSalary.divide(BigDecimal.valueOf(12));
        return monthSalary.multiply(BigDecimal.valueOf(this.overheadRate));
    }

    public Money calculateSgaCost(Money annualSalary) {
        Money monthSalary = annualSalary.divide(BigDecimal.valueOf(12));
        return monthSalary.multiply(BigDecimal.valueOf(this.sgaRate));
    }

}
