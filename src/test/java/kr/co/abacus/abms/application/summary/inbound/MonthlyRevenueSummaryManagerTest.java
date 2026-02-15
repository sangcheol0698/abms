package kr.co.abacus.abms.application.summary.inbound;

import static kr.co.abacus.abms.domain.project.ProjectFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import kr.co.abacus.abms.application.project.outbound.ProjectRevenuePlanRepository;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanCreateRequest;
import kr.co.abacus.abms.domain.project.RevenueType;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.shared.Period;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("월별 매출 집계 (MonthlyRevenueSummaryManager)")
class MonthlyRevenueSummaryManagerTest extends IntegrationTestBase {

    @Autowired
    private MonthlyRevenueSummaryManager monthlyRevenueSummaryManager;

    @Autowired
    private ProjectRevenuePlanRepository projectRevenuePlanRepository;

    @Test
    @DisplayName("매출 계산: 해당 월에 발행된 매출 계획들의 금액을 모두 합산한다")
    void calculateRevenue_Success() {
        // given
        Project project = createProject();
        ReflectionTestUtils.setField(project, "id", 1L);
        ReflectionTestUtils.setField(project, "period", new Period(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 20)));

        // 1000원짜리 계획과 2000원짜리 계획이 있다고 가정
        ProjectRevenuePlan plan1 = createProjectRevenuePlan(
            project.getId(),
            1,
            LocalDate.of(2026, 1, 1),
            RevenueType.DOWN_PAYMENT,
            1000L
        );
        ProjectRevenuePlan plan2 = createProjectRevenuePlan(
            project.getId(),
            2,
            LocalDate.of(2026, 2, 10),
            RevenueType.BALANCE_PAYMENT,
            2000L
        );

        plan1 = projectRevenuePlanRepository.save(plan1);
        plan2 = projectRevenuePlanRepository.save(plan2);

        plan1.issue();
        plan2.issue();

        flushAndClear();

        // when
        LocalDate targetMonth = LocalDate.of(2026, 2, 15); // 2월 기준
        Money result = monthlyRevenueSummaryManager.calculateRevenue(project, targetMonth);
        System.out.println("result = " + result);

        // then
        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(2000L));
    }

    @Test
    @DisplayName("매출 계산: 해당 월에 발행된 매출 계획이 없거나(다른 달/미발행), 데이터가 아예 없으면 0원을 반환한다")
    void calculateRevenue_Empty() {
        // given
        Project project = createProject();
        ReflectionTestUtils.setField(project, "id", 1L);
        // 기간 설정 (테스트 로직상 크게 중요하진 않으나 정합성 유지)
        ReflectionTestUtils.setField(project, "period", new Period(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));

        // Case 1: 1월(다른 달)에 발행된 매출 계획 -> 2월 집계 대상 아님
        ProjectRevenuePlan otherMonthPlan = createProjectRevenuePlan(
            project.getId(),
            1,
            LocalDate.of(2026, 1, 15),
            RevenueType.DOWN_PAYMENT,
            1000L
        );
        projectRevenuePlanRepository.save(otherMonthPlan);
        otherMonthPlan.issue(); // 발행 처리

        // Case 2: 2월(이번 달)이지만 아직 발행 안 된(isIssued=false) 매출 계획 -> 집계 대상 아님
        ProjectRevenuePlan unissuedPlan = createProjectRevenuePlan(
            project.getId(),
            2,
            LocalDate.of(2026, 2, 15),
            RevenueType.BALANCE_PAYMENT,
            5000L
        );
        projectRevenuePlanRepository.save(unissuedPlan);

        flushAndClear();

        // when
        LocalDate targetMonth = LocalDate.of(2026, 2, 20); // 2월 기준 조회
        Money result = monthlyRevenueSummaryManager.calculateRevenue(project, targetMonth);

        // then
        // 1월 발행건(1000원) 제외, 2월 미발행건(5000원) 제외 -> 결과는 0원이어야 함
        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private ProjectRevenuePlan createProjectRevenuePlan(Long projectId, Integer sequence, LocalDate revenueDate, RevenueType type, Long amount) {
        return ProjectRevenuePlan.create(
            new ProjectRevenuePlanCreateRequest(
                projectId,
                sequence,
                revenueDate,
                type,
                amount,
                null
            )
        );
    }
}