package kr.co.abacus.abms.domain.project;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.shared.Period;

@DisplayName("프로젝트 매출 계획 (ProjectRevenuePlan)")
class ProjectRevenuePlanTest {

    @Test
    @DisplayName("프로젝트 매출 계획 생성 - 필수 정보와 초기 상태")
    void create() {
        ProjectRevenuePlan projectRevenuePlan = ProjectRevenuePlan.create(
            new ProjectRevenuePlanCreateRequest(
                1L,
                1,
                LocalDate.of(2024, 1, 1),
                RevenueType.DOWN_PAYMENT,
                100000000L,
                "메모~"
            )
        );

        assertThat(projectRevenuePlan.getProjectId()).isEqualTo(1L);
        assertThat(projectRevenuePlan.getSequence()).isEqualTo(1);
        assertThat(projectRevenuePlan.getRevenueDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(projectRevenuePlan.getType()).isEqualTo(RevenueType.DOWN_PAYMENT);
        assertThat(projectRevenuePlan.getAmount()).isEqualTo(Money.wons(100000000L));
        assertThat(projectRevenuePlan.getMemo()).isEqualTo("메모~");
    }

}