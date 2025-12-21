package kr.co.abacus.abms.application.project.outbound;

import static kr.co.abacus.abms.domain.project.ProjectFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.support.IntegrationTestBase;

class ProjectRepositoryTest extends IntegrationTestBase {

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    @DisplayName("프로젝트 저장")
    void save() {
        Project project = createProject();

        Project savedProject = projectRepository.save(project);
        flushAndClear();

        assertThat(savedProject.getId()).isNotNull();
        assertThat(savedProject.getCode()).isEqualTo("PRJ-001");
    }

    @Test
    @DisplayName("프로젝트 ID로 조회")
    void findById() {
        Project project = createProject();
        projectRepository.save(project);
        flushAndClear();

        Optional<Project> found = projectRepository.findById(project.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("PRJ-001");
    }

    @Test
    @DisplayName("삭제되지 않은 프로젝트 ID로 조회")
    void findByIdAndDeletedFalse() {
        Project project = createProject();
        projectRepository.save(project);
        flushAndClear();

        Optional<Project> found = projectRepository.findByIdAndDeletedFalse(project.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("PRJ-001");
    }

    @Test
    @DisplayName("soft delete된 프로젝트는 조회되지 않음")
    void findByIdAndDeletedFalse_excludesDeleted() {
        Project project = createProject();
        projectRepository.save(project);
        flushAndClear();

        Project toDelete = projectRepository.findById(project.getId()).orElseThrow();
        toDelete.softDelete("testUser");
        projectRepository.save(toDelete);
        flushAndClear();

        Optional<Project> found = projectRepository.findByIdAndDeletedFalse(project.getId());

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("삭제되지 않은 모든 프로젝트 조회")
    void findAllByDeletedFalse() {
        Project project1 = createProject("PRJ-001");
        Project project2 = createProject("PRJ-002");
        projectRepository.save(project1);
        projectRepository.save(project2);
        flushAndClear();

        List<Project> projects = projectRepository.findAllByDeletedFalse();

        assertThat(projects).hasSize(2);
    }

    @Test
    @DisplayName("삭제되지 않은 프로젝트 페이징 조회")
    void findAllByDeletedFalse_paging() {
        for (int i = 1; i <= 15; i++) {
            projectRepository.save(createProject("PRJ-" + String.format("%03d", i)));
        }
        flushAndClear();

        Page<Project> page = projectRepository.findAllByDeletedFalse(PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(15);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("프로젝트 코드 존재 여부 확인")
    void existsByCode() {
        Project project = createProject("PRJ-001");
        projectRepository.save(project);
        flushAndClear();

        boolean exists = projectRepository.existsByCode("PRJ-001");
        boolean notExists = projectRepository.existsByCode("PRJ-999");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("거래처 ID로 프로젝트 목록 조회")
    void findAllByPartyIdAndDeletedFalse() {
        Long partyId = 1L;
        Long otherPartyId = 2L;

        projectRepository.save(Project.create(createProjectCreateRequest("PRJ-001", "프로젝트1", partyId)));
        projectRepository.save(Project.create(createProjectCreateRequest("PRJ-002", "프로젝트2", partyId)));
        projectRepository.save(Project.create(createProjectCreateRequest("PRJ-003", "프로젝트3", otherPartyId)));
        flushAndClear();

        List<Project> projects = projectRepository.findAllByPartyIdAndDeletedFalse(partyId);

        assertThat(projects).hasSize(2);
        assertThat(projects).extracting(Project::getCode)
                .containsExactlyInAnyOrder("PRJ-001", "PRJ-002");
    }

    @Test
    @DisplayName("상태별 프로젝트 목록 조회")
    void findAllByStatusAndDeletedFalse() {
        projectRepository.save(createProject("PRJ-001")); // IN_PROGRESS
        projectRepository.save(createProject("PRJ-002")); // IN_PROGRESS

        Project completedProject = createProject("PRJ-003");
        completedProject.complete();
        projectRepository.save(completedProject);
        flushAndClear();

        List<Project> inProgressProjects = projectRepository.findAllByStatusAndDeletedFalse(ProjectStatus.IN_PROGRESS);
        List<Project> completedProjects = projectRepository.findAllByStatusAndDeletedFalse(ProjectStatus.COMPLETED);

        assertThat(inProgressProjects).hasSize(2);
        assertThat(completedProjects).hasSize(1);
    }

}
