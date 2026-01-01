package kr.co.abacus.abms.domain.positionhistory;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.shared.Period;

class PositionHistoryTest {
    @Test
    void create() {
        PositionHistory positionHistory = PositionHistory.create(1L, new Period(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1)), EmployeePosition.ASSOCIATE);

        assertThat(positionHistory.getEmployeeId()).isNotNull();
        assertThat(positionHistory.getPeriod().startDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(positionHistory.getPeriod().endDate()).isEqualTo(LocalDate.of(2027, 1, 1));
        assertThat(positionHistory.getPosition()).isEqualTo(EmployeePosition.ASSOCIATE);
    }
}