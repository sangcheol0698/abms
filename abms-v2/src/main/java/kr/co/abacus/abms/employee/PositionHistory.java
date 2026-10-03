package kr.co.abacus.abms.employee;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;

import kr.co.abacus.abms.common.domain.BaseEntity;
import kr.co.abacus.abms.common.domain.Period;

/**
 * 직급/등급 변경 이력.
 */
@Entity
@Table(name = "tb_position_history")
@SQLRestriction("deleted = false")
public class PositionHistory extends BaseEntity {

    @Column(nullable = false)
    private Long employeeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmployeePosition position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmployeeGrade grade;

    @Embedded
    private Period period;

    protected PositionHistory() {
    }

    public static PositionHistory start(Long employeeId, EmployeePosition position, EmployeeGrade grade, LocalDate startDate) {
        PositionHistory history = new PositionHistory();
        history.employeeId = employeeId;
        history.position = position;
        history.grade = grade;
        history.period = new Period(startDate, null);
        return history;
    }

    public void closeAt(LocalDate endDate) {
        LocalDate end = endDate.isBefore(period.startDate()) ? period.startDate() : endDate;
        this.period = new Period(period.startDate(), end);
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public EmployeePosition getPosition() {
        return position;
    }

    public EmployeeGrade getGrade() {
        return grade;
    }

    public Period getPeriod() {
        return period;
    }

}
