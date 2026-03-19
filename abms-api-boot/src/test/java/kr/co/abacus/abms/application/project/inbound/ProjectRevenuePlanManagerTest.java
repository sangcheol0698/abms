package kr.co.abacus.abms.application.project.inbound;

import static kr.co.abacus.abms.domain.project.ProjectRevenuePlanFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.project.outbound.ProjectRevenuePlanRepository;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanCreateRequest;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanDuplicateException;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanNotFoundException;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanUpdateRequest;
import kr.co.abacus.abms.domain.project.RevenueType;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("프로젝트 매출 계획 수정 (ProjectRevenuePlanFinder)")
class ProjectRevenuePlanManagerTest extends IntegrationTestBase {

    @Autowired
    private ProjectRevenuePlanManager projectRevenuePlanManager;

    @Autowired
    private ProjectRevenuePlanRepository projectRevenuePlanRepository;

    @Test
    @DisplayName("프로젝트 매출 계획 생성")
    void create() {
        Long projectId = 1L;

        ProjectRevenuePlan projectRevenuePlan = projectRevenuePlanManager.create(
                createProjectRevenuePlanCreateRequest(projectId, 1)
        );

        Optional<ProjectRevenuePlan> foundRevenuePlan = projectRevenuePlanRepository.findByProjectIdAndSequence(projectRevenuePlan.getProjectId(), projectRevenuePlan.getSequence());
        assertThat(foundRevenuePlan.get().getProjectId()).isEqualTo(projectId);
    }

    @Test
    @DisplayName("동일 프로젝트 내 매출 계획의 순서는 중복될 수 없음")
    void create_fail_duplicateOrder() {
        Long projectId = 1L;
        Integer sequence = 1;

        projectRevenuePlanManager.create(createProjectRevenuePlanCreateRequest(projectId, sequence));

        ProjectRevenuePlanCreateRequest createRequest = createProjectRevenuePlanCreateRequest(projectId, sequence);

        assertThatThrownBy(() -> projectRevenuePlanManager.create(createRequest)).isInstanceOf(ProjectRevenuePlanDuplicateException.class);
    }

    @Test
    @DisplayName("프로젝트 매출 계획 수정")
    void update() {
        Long projectId = 1L;
        projectRevenuePlanRepository.save(createProjectRevenuePlan(projectId, 1));
        flushAndClear();

        ProjectRevenuePlan updated = projectRevenuePlanManager.update(
                projectId,
                1,
                new ProjectRevenuePlanUpdateRequest(
                        2,
                        LocalDate.of(2026, 2, 15),
                        RevenueType.BALANCE_PAYMENT,
                        20_000_000L,
                        "수정 메모"
                )
        );

        Optional<ProjectRevenuePlan> foundRevenuePlan = projectRevenuePlanRepository.findByProjectIdAndSequence(projectId, 2);
        assertThat(updated.getSequence()).isEqualTo(2);
        assertThat(foundRevenuePlan).isPresent();
        assertThat(foundRevenuePlan.get().getRevenueDate()).isEqualTo(LocalDate.of(2026, 2, 15));
        assertThat(foundRevenuePlan.get().getType()).isEqualTo(RevenueType.BALANCE_PAYMENT);
        assertThat(foundRevenuePlan.get().getAmount().amount().longValue()).isEqualTo(20_000_000L);
        assertThat(foundRevenuePlan.get().getMemo()).isEqualTo("수정 메모");
    }

    @Test
    @DisplayName("자기 자신의 회차를 유지하는 수정은 허용한다")
    void update_keepSameSequence() {
        Long projectId = 1L;
        projectRevenuePlanRepository.save(createProjectRevenuePlan(projectId, 1));
        flushAndClear();

        ProjectRevenuePlan updated = projectRevenuePlanManager.update(
                projectId,
                1,
                new ProjectRevenuePlanUpdateRequest(
                        1,
                        LocalDate.of(2026, 3, 1),
                        RevenueType.INTERMEDIATE_PAYMENT,
                        15_000_000L,
                        "동일 회차"
                )
        );

        assertThat(updated.getSequence()).isEqualTo(1);
        assertThat(updated.getRevenueDate()).isEqualTo(LocalDate.of(2026, 3, 1));
    }

    @Test
    @DisplayName("다른 일정과 회차가 중복되면 수정할 수 없다")
    void update_fail_duplicateSequence() {
        Long projectId = 1L;
        projectRevenuePlanRepository.save(createProjectRevenuePlan(projectId, 1));
        projectRevenuePlanRepository.save(createProjectRevenuePlan(projectId, 2));
        flushAndClear();

        assertThatThrownBy(() -> projectRevenuePlanManager.update(
                projectId,
                1,
                new ProjectRevenuePlanUpdateRequest(
                        2,
                        LocalDate.of(2026, 2, 1),
                        RevenueType.ETC,
                        5_000_000L,
                        null
                )
        )).isInstanceOf(ProjectRevenuePlanDuplicateException.class);
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 매출 계획은 수정할 수 없다")
    void update_fail_notFound() {
        assertThatThrownBy(() -> projectRevenuePlanManager.update(
                1L,
                99,
                new ProjectRevenuePlanUpdateRequest(
                        99,
                        LocalDate.of(2026, 2, 1),
                        RevenueType.ETC,
                        5_000_000L,
                        null
                )
        )).isInstanceOf(ProjectRevenuePlanNotFoundException.class);
    }

    @Test
    @DisplayName("프로젝트 매출 계획을 발행 상태로 변경한다")
    void issue() {
        Long projectId = 1L;
        projectRevenuePlanRepository.save(createProjectRevenuePlan(projectId, 1));
        flushAndClear();

        ProjectRevenuePlan issued = projectRevenuePlanManager.issue(projectId, 1);

        assertThat(issued.getIsIssued()).isTrue();
    }

    @Test
    @DisplayName("프로젝트 매출 계획의 발행 상태를 취소한다")
    void cancel() {
        Long projectId = 1L;
        ProjectRevenuePlan projectRevenuePlan = createProjectRevenuePlan(projectId, 1);
        projectRevenuePlan.issue();
        projectRevenuePlanRepository.save(projectRevenuePlan);
        flushAndClear();

        ProjectRevenuePlan cancelled = projectRevenuePlanManager.cancel(projectId, 1);

        assertThat(cancelled.getIsIssued()).isFalse();
    }

}
