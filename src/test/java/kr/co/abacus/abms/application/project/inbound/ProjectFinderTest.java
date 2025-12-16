package kr.co.abacus.abms.application.project.inbound;

import static kr.co.abacus.abms.domain.project.ProjectFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectNotFoundException;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.support.IntegrationTestBase;

class ProjectFinderTest extends IntegrationTestBase {

    @Autowired
    private ProjectFinder projectFinder;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    @DisplayName("프로젝트 ID로 조회")
    void find() {
        Project project = createProject();
        projectRepository.save(project);
        flushAndClear();

        Project found = projectFinder.find(project.getId());

        assertThat(found.getId()).isEqualTo(project.getId());
        assertThat(found.getCode()).isEqualTo("PRJ-001");
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 조회 시 예외 발생")
    void find_notFound() {
        UUID nonExistentId = UUID.randomUUID();

        assertThatThrownBy(() -> projectFinder.find(nonExistentId))
            .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    @DisplayName("모든 프로젝트 조회")
    void findAll() {
        projectRepository.save(createProject("PRJ-001"));
        projectRepository.save(createProject("PRJ-002"));
        flushAndClear();

        List<Project> projects = projectFinder.findAll();

        assertThat(projects).hasSize(2);
    }

    @Test
    @DisplayName("모든 프로젝트 페이징 조회")
    void findAll_paging() {
        for (int i = 1; i <= 15; i++) {
            projectRepository.save(createProject("PRJ-" + String.format("%03d", i)));
        }
        flushAndClear();

        Page<Project> page = projectFinder.findAll(PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(15);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("거래처 ID로 프로젝트 조회")
    void findAllByPartyId() {
        UUID partyId = UUID.randomUUID();
        UUID otherPartyId = UUID.randomUUID();

        projectRepository.save(Project.create(createProjectCreateRequest("PRJ-001", "프로젝트1", partyId)));
        projectRepository.save(Project.create(createProjectCreateRequest("PRJ-002", "프로젝트2", partyId)));
        projectRepository.save(Project.create(createProjectCreateRequest("PRJ-003", "프로젝트3", otherPartyId)));
        flushAndClear();

        List<Project> projects = projectFinder.findAllByPartyId(partyId);

        assertThat(projects).hasSize(2);
    }

    @Test
    @DisplayName("상태별 프로젝트 조회")
    void findAllByStatus() {
        projectRepository.save(createProject("PRJ-001")); // IN_PROGRESS
        projectRepository.save(createProject("PRJ-002")); // IN_PROGRESS

        Project completedProject = createProject("PRJ-003");
        completedProject.complete();
        projectRepository.save(completedProject);
        flushAndClear();

        List<Project> inProgressProjects = projectFinder.findAllByStatus(ProjectStatus.IN_PROGRESS);
        List<Project> completedProjects = projectFinder.findAllByStatus(ProjectStatus.COMPLETED);

        assertThat(inProgressProjects).hasSize(2);
        assertThat(completedProjects).hasSize(1);
    }

}
