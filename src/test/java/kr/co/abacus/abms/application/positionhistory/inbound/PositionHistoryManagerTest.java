package kr.co.abacus.abms.application.positionhistory.inbound;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.domain.positionhistory.PositionHistoryCreateRequest;
import kr.co.abacus.abms.application.positionhistory.outbound.PositionHistoryRepository;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.positionhistory.PositionHistory;
import kr.co.abacus.abms.domain.shared.Period;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("직급 이력 관리 (PositionHistoryManager)")
class PositionHistoryManagerTest extends IntegrationTestBase {

    @Autowired
    private PositionHistoryManager positionHistoryManager;

    @Autowired
    private PositionHistoryRepository positionHistoryRepository;

    @DisplayName("직급 이력을 생성한다")
    @Test
    void create() {
        PositionHistory positionHistory = positionHistoryManager.create(new PositionHistoryCreateRequest(
            1L,
            new Period(LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1)),
            EmployeePosition.ASSOCIATE)
        );

        flushAndClear();

        assertThat(positionHistory.getEmployeeId()).isEqualTo(1L);
        assertThat(positionHistory.getPeriod().startDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(positionHistory.getPeriod().endDate()).isEqualTo(LocalDate.of(2027, 1, 1));
        assertThat(positionHistory.getPosition()).isEqualTo(EmployeePosition.ASSOCIATE);
    }

}
