package kr.co.abacus.abms.domain.positionhistory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import kr.co.abacus.abms.application.positionhistory.dto.PositionHistoryCreateRequest;
import kr.co.abacus.abms.domain.AbstractEntity;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.payroll.Payroll;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.shared.Period;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "position_history")
public class PositionHistory extends AbstractEntity {

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    // 이력 관리 테이블이라 수정을 못하게 할 필요가 있음
    // period 대신 발생 타임스탬프만 넣게할 수도 있음
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
