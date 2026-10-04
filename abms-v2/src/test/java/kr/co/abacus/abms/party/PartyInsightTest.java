package kr.co.abacus.abms.party;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.common.domain.Period;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.project.ProjectRevenuePlan;
import kr.co.abacus.abms.project.ProjectRevenuePlan.RevenuePlanInfo;
import kr.co.abacus.abms.project.ProjectStatus;
import kr.co.abacus.abms.project.RevenueType;

class PartyInsightTest {

    @Test
    void 발행_미발행_미계획_금액과_청구_일정을_계산한다() {
        Project project = withId(Project.create("P-1", new Project.ProjectInfo(1L, 1L, "프로젝트", null, ProjectStatus.IN_PROGRESS,
                Money.wons(1_000), new Period(LocalDate.of(2025, 6, 1), LocalDate.of(2026, 12, 31)))), 10L);
        ProjectRevenuePlan issuedLastYear = plan(1, LocalDate.of(2025, 12, 10), 300, true);
        ProjectRevenuePlan issuedThisYear = plan(2, LocalDate.of(2026, 3, 10), 200, true);
        ProjectRevenuePlan overdue = plan(3, LocalDate.of(2026, 4, 1), 100, false);
        ProjectRevenuePlan farFuture = plan(4, LocalDate.of(2026, 12, 1), 100, false);
        ProjectRevenuePlan otherProject = withId(ProjectRevenuePlan.create(99L,
                new RevenuePlanInfo(1, LocalDate.of(2026, 4, 1), RevenueType.ETC, Money.wons(5_000), null)), 99L);

        PartyInsight insight = PartyInsight.of(List.of(project), List.of(issuedLastYear, issuedThisYear, overdue, farFuture, otherProject),
                LocalDate.of(2026, 4, 10));

        assertThat(insight.totalContract()).isEqualTo(Money.wons(1_000));
        assertThat(insight.issuedTotal()).isEqualTo(Money.wons(500));
        assertThat(insight.unissuedPlanned()).isEqualTo(Money.wons(200));
        assertThat(insight.unplanned()).isEqualTo(Money.wons(300));
        assertThat(insight.thisYearIssued()).isEqualTo(Money.wons(200));
        assertThat(insight.upcoming()).extracting(u -> u.plan().getSequence()).containsExactly(3);
        assertThat(insight.upcoming().getFirst().overdue()).isTrue();
        assertThat(insight.yearly()).hasSize(5);
        assertThat(insight.yearly().getLast().amount()).isEqualTo(Money.wons(200));
        assertThat(insight.firstStartDate()).isEqualTo(LocalDate.of(2025, 6, 1));
    }

    private static ProjectRevenuePlan plan(int sequence, LocalDate date, long amount, boolean issued) {
        ProjectRevenuePlan plan = withId(ProjectRevenuePlan.create(10L, new RevenuePlanInfo(sequence, date, RevenueType.ETC, Money.wons(amount), null)),
                (long) sequence);
        if (issued) {
            plan.issue();
        }
        return plan;
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

}
