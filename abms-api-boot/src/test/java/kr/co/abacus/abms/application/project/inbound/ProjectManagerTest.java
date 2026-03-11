package kr.co.abacus.abms.application.project.inbound;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.project.dto.ProjectCreateCommand;
import kr.co.abacus.abms.application.project.dto.ProjectUpdateCommand;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.party.PartyCreateRequest;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("프로젝트 관리 (ProjectManager)")
class ProjectManagerTest extends IntegrationTestBase {

    @Autowired
    private ProjectManager projectManager;

    @Autowired
    private ProjectFinder projectFinder;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Test
    @DisplayName("command 기반으로 프로젝트를 생성한다")
    void create() {
        Long partyId = createParty("생성 협력사");

        Long projectId = projectManager.create(new ProjectCreateCommand(
                partyId,
                1L,
                "PRJ-CMD-001",
                "command 생성 프로젝트",
                "설명",
                ProjectStatus.SCHEDULED,
                100_000_000L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        ));
        flushAndClear();

        Project project = projectFinder.find(projectId);
        assertThat(project.getCode()).isEqualTo("PRJ-CMD-001");
        assertThat(project.getPartyId()).isEqualTo(partyId);
    }

    @Test
    @DisplayName("command 기반으로 프로젝트를 수정한다")
    void update() {
        Long oldPartyId = createParty("기존 협력사");
        Long newPartyId = createParty("변경 협력사");
        Project project = projectRepository.save(Project.create(
                oldPartyId,
                1L,
                "PRJ-CMD-UPDATE-001",
                "기존 프로젝트",
                "설명",
                ProjectStatus.IN_PROGRESS,
                100_000_000L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        ));
        flushAndClear();

        Long projectId = projectManager.update(project.getIdOrThrow(), new ProjectUpdateCommand(
                newPartyId,
                1L,
                "수정된 프로젝트",
                "수정 설명",
                ProjectStatus.ON_HOLD,
                150_000_000L,
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2025, 1, 31)
        ));
        flushAndClear();

        Project updated = projectFinder.find(projectId);
        assertThat(updated.getPartyId()).isEqualTo(newPartyId);
        assertThat(updated.getName()).isEqualTo("수정된 프로젝트");
        assertThat(updated.getStatus()).isEqualTo(ProjectStatus.ON_HOLD);
        assertThat(updated.getContractAmount().amount().longValue()).isEqualTo(150_000_000L);
        assertThat(updated.getPeriod().startDate()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(updated.getPeriod().endDate()).isEqualTo(LocalDate.of(2025, 1, 31));
    }

    private Long createParty(String name) {
        Party party = partyRepository.save(Party.create(new PartyCreateRequest(name, null, null, null, null)));
        return party.getIdOrThrow();
    }

}
