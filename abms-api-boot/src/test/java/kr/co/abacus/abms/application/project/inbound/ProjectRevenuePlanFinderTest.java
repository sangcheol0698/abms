package kr.co.abacus.abms.application.project.inbound;

import static kr.co.abacus.abms.domain.project.ProjectFixture.*;
import static kr.co.abacus.abms.domain.project.ProjectRevenuePlanFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import kr.co.abacus.abms.application.project.dto.ProjectSearchCondition;
import kr.co.abacus.abms.application.project.dto.ProjectSummary;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRevenuePlanRepository;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectCreateRequest;
import kr.co.abacus.abms.domain.project.ProjectNotFoundException;
import kr.co.abacus.abms.domain.project.ProjectRevenuePlan;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("프로젝트 매출 계획 조회 (ProjectRevenuePlanFinder)")
class ProjectRevenuePlanFinderTest extends IntegrationTestBase {

    @Autowired
    private ProjectRevenuePlanFinder projectRevenuePlanFinder;

    @Autowired
    private ProjectRevenuePlanRepository projectRevenuePlanRepository;

    @Test
    @DisplayName("프로젝트 ID로 매출 계획 조회")
    void findByProjectId() {
        Long projectId = 1L;

        ProjectRevenuePlan projectRevenuePlan = createProjectRevenuePlan(projectId, 1);
        projectRevenuePlanRepository.save(projectRevenuePlan);

        ProjectRevenuePlan projectRevenuePlan2 = createProjectRevenuePlan(projectId, 2);
        projectRevenuePlanRepository.save(projectRevenuePlan2);

        ProjectRevenuePlan projectRevenuePlan3 = createProjectRevenuePlan(projectId, 3);
        projectRevenuePlanRepository.save(projectRevenuePlan3);

        flushAndClear();

        List<ProjectRevenuePlan> foundRevenuePlan = projectRevenuePlanFinder.findByProjectId(projectId);
        assertThat(foundRevenuePlan).hasSize(3);
    }

    @Test
    @DisplayName("프로젝트 ID와 순서로 매출 계획 조회")
    void findByProjectIdAndSequence() {
        Long projectId = 1L;

        clear();

        ProjectRevenuePlan projectRevenuePlan = createProjectRevenuePlan(projectId, 1);
        projectRevenuePlanRepository.save(projectRevenuePlan);

        ProjectRevenuePlan projectRevenuePlan2 = createProjectRevenuePlan(projectId, 2);
        projectRevenuePlanRepository.save(projectRevenuePlan2);

        ProjectRevenuePlan projectRevenuePlan3 = createProjectRevenuePlan(projectId, 3);
        projectRevenuePlanRepository.save(projectRevenuePlan3);

        flushAndClear();

        ProjectRevenuePlan foundRevenuePlan = projectRevenuePlanFinder.findByProjectIdAndSequence(projectId, 1);
        assertThat(foundRevenuePlan.getProjectId()).isEqualTo(projectId);
        assertThat(foundRevenuePlan.getSequence()).isEqualTo(1);
    }

}
