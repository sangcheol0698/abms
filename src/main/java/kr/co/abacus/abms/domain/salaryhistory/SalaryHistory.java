package kr.co.abacus.abms.domain.salaryhistory;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import kr.co.abacus.abms.domain.AbstractEntity;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.shared.Period;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "salary_history")
public class SalaryHistory extends AbstractEntity {

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "annual_salary", nullable = false, precision = 10))
    private Money annualSalary;

    @Embedded
    @AttributeOverride(name = "startDate", column = @Column(name = "start_date", nullable = false))
    @AttributeOverride(name = "endDate", column = @Column(name = "end_date"))
    private Period period;

    public static SalaryHistory create(UUID employeeId, Money annualSalary, Period period) {
        SalaryHistory salaryHistory = new SalaryHistory();

        salaryHistory.employeeId = Objects.requireNonNull(employeeId);
        salaryHistory.annualSalary = Objects.requireNonNull(annualSalary);
        salaryHistory.period = Objects.requireNonNull(period);

        return salaryHistory;
    }

}
