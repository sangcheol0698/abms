package kr.co.abacus.abms.application.positionhistory.inbound;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import javax.swing.text.Position;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.positionhistory.dto.PositionHistoryCreateRequest;
import kr.co.abacus.abms.application.positionhistory.outbound.PositionHistoryRepository;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.positionhistory.PositionHistory;
import kr.co.abacus.abms.domain.shared.Period;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("직급 이력 조회 (PositionHistoryFinder)")
class PositionHistoryFinderTest extends IntegrationTestBase {

    @Autowired
    private PositionHistoryFinder positionHistoryFinder;

    @Autowired
    private PositionHistoryRepository positionHistoryRepository;

    @Test
    @DisplayName("직원 ID로 가장 마지막 직급 이력을 조회한다")
    void findLast() {
        PositionHistory positionHistory = PositionHistory.create(new PositionHistoryCreateRequest(
            1L,
            new Period(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)),
            EmployeePosition.ASSOCIATE)
        );
        positionHistoryRepository.save(positionHistory);

        PositionHistory positionHistory2 = PositionHistory.create(new PositionHistoryCreateRequest(
            1L,
            new Period(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)),
            EmployeePosition.ASSOCIATE)
        );
        positionHistoryRepository.save(positionHistory2);

        flush();

        PositionHistory foundPositionHistory = positionHistoryFinder.findLast(1L);

        assertThat(foundPositionHistory.getEmployeeId()).isEqualTo(1L);
        assertThat(foundPositionHistory.getPeriod().startDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(foundPositionHistory.getPeriod().endDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

}
