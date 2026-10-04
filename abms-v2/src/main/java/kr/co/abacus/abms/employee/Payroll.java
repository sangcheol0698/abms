package kr.co.abacus.abms.employee;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;

import kr.co.abacus.abms.common.audit.Auditable;
import kr.co.abacus.abms.common.audit.Auditable.AuditRef;
import kr.co.abacus.abms.common.domain.BaseEntity;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.common.domain.Period;

/**
 * 직원 연봉 이력. 같은 직원의 이력은 기간이 겹치지 않으며, 새 연봉이 등록되면 이전 이력이 종료된다.
 */
@Entity
@Table(name = "tb_payroll")
@SQLRestriction("deleted = false")
public class Payroll extends BaseEntity implements Auditable {

    @Column(nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private Money annualSalary;

    @Embedded
    private Period period;

    protected Payroll() {
    }

    public static Payroll start(Long employeeId, Money annualSalary, LocalDate startDate) {
        if (annualSalary.isNegative() || annualSalary.equals(Money.ZERO)) {
            throw new BusinessException("연봉은 0원보다 커야 합니다.");
        }
        Payroll payroll = new Payroll();
        payroll.employeeId = Objects.requireNonNull(employeeId);
        payroll.annualSalary = annualSalary;
        payroll.period = new Period(startDate, null);
        return payroll;
    }

    public void closeAt(LocalDate endDate) {
        this.period = new Period(period.startDate(), endDate);
    }

    public Money monthlySalary() {
        return annualSalary.dividedBy(java.math.BigDecimal.valueOf(12));
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public Money getAnnualSalary() {
        return annualSalary;
    }

    public Period getPeriod() {
        return period;
    }

    @Override
    public String auditLabel() {
        return "연봉";
    }

    @Override
    public String auditName() {
        return "연봉";
    }

    @Override
    public AuditRef auditParent() {
        return new AuditRef("Employee", employeeId);
    }

}
