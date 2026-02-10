package kr.co.abacus.abms.application.project.inbound;

import static kr.co.abacus.abms.domain.project.ProjectRevenuePlanFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.project.outbound.ProjectRevenuePlanRepository;
import kr.co.abacus.abms.domain.project.ProjectNotFoundException;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanCreateRequest;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlanDuplicateException;
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

}
