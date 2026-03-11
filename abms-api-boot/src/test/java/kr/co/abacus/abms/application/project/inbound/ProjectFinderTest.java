package kr.co.abacus.abms.application.project.inbound;

import static kr.co.abacus.abms.domain.project.ProjectFixture.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.project.dto.ProjectSearchCondition;
import kr.co.abacus.abms.application.project.dto.ProjectSummary;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.party.PartyCreateRequest;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectNotFoundException;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("프로젝트 조회 (ProjectFinder)")
class ProjectFinderTest extends IntegrationTestBase {

    @Autowired
    private ProjectFinder projectFinder;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PartyRepository partyRepository;

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
        Long nonExistentId = 9999L;

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
        Long partyId = 1L;
        Long otherPartyId = 2L;
        Long leadDepartmentId = 1L;

        projectRepository.save(createProject("PRJ-001", "프로젝트1", partyId, leadDepartmentId));
        projectRepository.save(createProject("PRJ-002", "프로젝트2", partyId, leadDepartmentId));
        projectRepository.save(createProject("PRJ-003", "프로젝트3", otherPartyId, leadDepartmentId));
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

        Page<ProjectSummary> projects = projectFinder.search(condition, PageRequest.of(0, 10));

        assertThat(projects).hasSize(1)
                .extracting(ProjectSummary::code, ProjectSummary::partyName)
                .containsExactly(tuple("PRJ-ALPHA-001", "알파 협력사"));
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
