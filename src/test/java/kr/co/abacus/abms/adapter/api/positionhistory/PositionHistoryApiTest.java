package kr.co.abacus.abms.adapter.api.positionhistory;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;

import kr.co.abacus.abms.domain.positionhistory.PositionHistoryCreateRequest;
import kr.co.abacus.abms.application.positionhistory.inbound.PositionHistoryManager;
import kr.co.abacus.abms.application.positionhistory.outbound.PositionHistoryRepository;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.positionhistory.PositionHistory;
import kr.co.abacus.abms.domain.shared.Period;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("직급 이력 API (PositionHistoryApi)")
class PositionHistoryApiTest extends ApiIntegrationTestBase {

    @Autowired
    private PositionHistoryRepository positionHistoryRepository;

    @Autowired
    private PositionHistoryManager positionHistoryManager;

    @Test
    @DisplayName("직원의 모든 직급 이력을 조회한다")
    void findAll() {
        PositionHistory positionHistory = PositionHistory.create(new PositionHistoryCreateRequest(
                1L,
                new Period(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)),
                EmployeePosition.ASSOCIATE)
        );
        positionHistoryRepository.save(positionHistory);

        PositionHistory positionHistory2 = PositionHistory.create(new PositionHistoryCreateRequest(
                1L,
                new Period(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)),
                EmployeePosition.SENIOR_ASSOCIATE)
        );
        positionHistoryRepository.save(positionHistory2);

        flushAndClear();

        List<PositionHistory> response = restTestClient.get()
                .uri("/api/positionHistory/{employeeId}", 1L)
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<PositionHistory>>() {
                })
                .returnResult()
                .getResponseBody();

        System.out.println("response:" + response);
        assertThat(response).isNotNull();
        assertThat(response.size()).isEqualTo(2);
        assertThat(response.getFirst().getPosition()).isEqualTo(EmployeePosition.ASSOCIATE);
        assertThat(response.getLast().getPosition()).isEqualTo(EmployeePosition.SENIOR_ASSOCIATE);
    }

}