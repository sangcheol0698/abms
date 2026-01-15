package kr.co.abacus.abms.domain.positionhistory;

import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.AbstractEntity;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.shared.Period;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "position_history")
public class PositionHistory extends AbstractEntity {

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Embedded
    @AttributeOverride(name = "startDate", column = @Column(name = "start_date", nullable = false))
    @AttributeOverride(name = "endDate", column = @Column(name = "end_date"))
    private Period period;

    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false, length = 20)
    private EmployeePosition position;

    public static PositionHistory create(PositionHistoryCreateRequest createRequest) {
        PositionHistory positionHistory = new PositionHistory();

        positionHistory.employeeId = Objects.requireNonNull(createRequest.employeeId());
        positionHistory.period = new Period(Objects.requireNonNull(createRequest.period().startDate()), createRequest.period().endDate());
        positionHistory.position = Objects.requireNonNull(createRequest.position());

        return positionHistory;
    }
}
