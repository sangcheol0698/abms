package kr.co.abacus.abms.application.project.outbound;

import static kr.co.abacus.abms.domain.project.ProjectFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.project.dto.ProjectOverviewSummary;
import kr.co.abacus.abms.application.project.dto.ProjectSearchCondition;
import kr.co.abacus.abms.application.project.dto.ProjectSummary;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.party.PartyCreateRequest;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.support.IntegrationTestBase;

class ProjectRepositoryTest extends IntegrationTestBase {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PartyRepository partyRepository;

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
        toDelete.softDelete(1L);
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
        Long leadDepartmentId = 1L;

        projectRepository.save(createProject("PRJ-001", "프로젝트1", partyId, leadDepartmentId));
        projectRepository.save(createProject("PRJ-002", "프로젝트2", partyId, leadDepartmentId));
        projectRepository.save(createProject("PRJ-003", "프로젝트3", otherPartyId, leadDepartmentId));
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

    @Test
    @DisplayName("프로젝트 조건에 따른 검색")
    void search() {
        Long alphaPartyId = createParty("알파 협력사");
        Long betaPartyId = createParty("베타 협력사");
        projectRepository.save(createProjectForSearch("PRJ-ALPHA-001", "알파 프로젝트", alphaPartyId, 1L, ProjectStatus.IN_PROGRESS,
                LocalDate.of(2023, 12, 20), LocalDate.of(2024, 1, 15)));
        projectRepository.save(createProjectForSearch("PRJ-ALPHA-002", "알파 보조", alphaPartyId, 1L, ProjectStatus.COMPLETED,
                LocalDate.of(2024, 2, 5), LocalDate.of(2024, 2, 28)));
        projectRepository.save(createProjectForSearch("PRJ-ALPHA-003", "알파 프로젝트", betaPartyId, 1L, ProjectStatus.IN_PROGRESS,
                LocalDate.of(2024, 3, 1), LocalDate.of(2024, 8, 31)));
        projectRepository.save(createProjectForSearch("PRJ-ALPHA-004", "알파 프로젝트", alphaPartyId, 1L, ProjectStatus.IN_PROGRESS,
                LocalDate.of(2023, 5, 1), LocalDate.of(2023, 12, 15)));
        flushAndClear();

        ProjectSearchCondition condition = new ProjectSearchCondition(
                "알파",
                List.of(ProjectStatus.IN_PROGRESS),
                List.of(alphaPartyId),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31));

        Page<ProjectSummary> projects = projectRepository.search(condition, PageRequest.of(0, 10));

        assertThat(projects).hasSize(1)
                .extracting(ProjectSummary::code, ProjectSummary::partyName)
                .containsExactly(tuple("PRJ-ALPHA-001", "알파 협력사"));
    }

    @Test
    @DisplayName("프로젝트 요약 정보를 집계한다")
    void summarize() {
        Long summaryPartyId = createParty("요약 협력사");
        Long otherPartyId = createParty("다른 협력사");
        projectRepository.save(createProjectForSearch("PRJ-SUM-001", "요약 프로젝트 1", summaryPartyId, 1L, ProjectStatus.SCHEDULED,
                LocalDate.of(2024, 1, 10), LocalDate.of(2024, 6, 30)));
        projectRepository.save(createProjectForSearch("PRJ-SUM-002", "요약 프로젝트 2", summaryPartyId, 1L, ProjectStatus.IN_PROGRESS,
                LocalDate.of(2024, 2, 10), LocalDate.of(2024, 7, 31)));
        projectRepository.save(createProjectForSearch("PRJ-SUM-003", "요약 프로젝트 3", summaryPartyId, 1L, ProjectStatus.COMPLETED,
                LocalDate.of(2024, 3, 10), LocalDate.of(2024, 8, 31)));
        projectRepository.save(createProjectForSearch("PRJ-SUM-004", "요약 프로젝트 4", summaryPartyId, 1L, ProjectStatus.ON_HOLD,
                LocalDate.of(2024, 4, 10), LocalDate.of(2024, 9, 30)));
        projectRepository.save(createProjectForSearch("PRJ-SUM-005", "요약 프로젝트 5", otherPartyId, 1L, ProjectStatus.CANCELLED,
                LocalDate.of(2024, 5, 10), LocalDate.of(2024, 10, 31)));
        flushAndClear();

        ProjectOverviewSummary summary = projectRepository.summarize(new ProjectSearchCondition(
                "요약",
                null,
                List.of(summaryPartyId),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        ));

        assertThat(summary.totalCount()).isEqualTo(4);
        assertThat(summary.scheduledCount()).isEqualTo(1);
        assertThat(summary.inProgressCount()).isEqualTo(1);
        assertThat(summary.completedCount()).isEqualTo(1);
        assertThat(summary.onHoldCount()).isEqualTo(1);
        assertThat(summary.cancelledCount()).isEqualTo(0);
        assertThat(summary.totalContractAmount()).isEqualTo(400_000_000L);
    }

    private Project createProjectForSearch(String code, String name, Long partyId, Long leadDepartmentId, ProjectStatus status,
                                           LocalDate startDate, LocalDate endDate) {
        return Project.create(
                partyId,
                leadDepartmentId,
                code,
                name,
                "테스트 프로젝트 설명",
                status,
                100_000_000L,
                startDate,
                endDate);
    }

    private Long createParty(String name) {
        Party party = partyRepository.save(Party.create(new PartyCreateRequest(name, null, null, null, null)));
        return party.getIdOrThrow();
    }

}
