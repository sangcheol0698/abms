package kr.co.abacus.abms.domain.payroll;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.AbstractEntity;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.shared.Period;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "payroll")
public class Payroll extends AbstractEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "annual_salary", nullable = false))
    private Money annualSalary;

    @Embedded
    @AttributeOverride(name = "startDate", column = @Column(name = "start_date", nullable = false))
    @AttributeOverride(name = "endDate", column = @Column(name = "end_date"))
    private Period period;

    public static Payroll startWith(UUID employeeId, Money annualSalary, LocalDate startDate) {
        Payroll payroll = new Payroll();

        payroll.employeeId = Objects.requireNonNull(employeeId);
        payroll.annualSalary = Objects.requireNonNull(annualSalary);
        payroll.period = new Period(Objects.requireNonNull(startDate), null);

        return payroll;
    }

    public void close(LocalDate endDate) {
        this.period = new Period(this.period.startDate(), endDate);
    }

}
