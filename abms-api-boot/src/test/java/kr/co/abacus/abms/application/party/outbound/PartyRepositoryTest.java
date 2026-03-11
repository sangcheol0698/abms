package kr.co.abacus.abms.application.party.outbound;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import kr.co.abacus.abms.application.party.dto.PartyListItem;
import kr.co.abacus.abms.application.party.dto.PartyOverviewSummary;
import kr.co.abacus.abms.application.party.dto.PartySearchCondition;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.party.PartyCreateRequest;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("협력사 저장소 (PartyRepository)")
class PartyRepositoryTest extends IntegrationTestBase {

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    @DisplayName("협력사 목록을 DTO로 조회한다")
    void search() {
        partyRepository.save(createParty("요약 협력사 A"));
        partyRepository.save(createParty("요약 협력사 B"));
        flushAndClear();

        Page<PartyListItem> result = partyRepository.search(PageRequest.of(0, 10), new PartySearchCondition("요약"));

        assertThat(result.getContent())
                .extracting(PartyListItem::name)
                .containsExactlyInAnyOrder("요약 협력사 A", "요약 협력사 B");
    }

    @Test
    @DisplayName("협력사 요약 정보를 집계한다")
    void summarize() {
        Party alpha = partyRepository.save(createParty("요약 협력사 Alpha"));
        Party beta = partyRepository.save(createParty("요약 협력사 Beta"));
        Party gamma = partyRepository.save(createParty("요약 협력사 Gamma"));

        projectRepository.save(createProject("PRJ-PARTY-SUM-001", alpha.getId(), ProjectStatus.IN_PROGRESS, 100_000_000L));
        projectRepository.save(createProject("PRJ-PARTY-SUM-002", beta.getId(), ProjectStatus.COMPLETED, 200_000_000L));
        projectRepository.save(createProject("PRJ-PARTY-SUM-003", beta.getId(), ProjectStatus.COMPLETED, 300_000_000L));
        flushAndClear();

        PartyOverviewSummary summary = partyRepository.summarize(new PartySearchCondition("요약"));

        assertThat(summary.totalCount()).isEqualTo(3);
        assertThat(summary.withProjectsCount()).isEqualTo(2);
        assertThat(summary.withInProgressProjectsCount()).isEqualTo(1);
        assertThat(summary.withoutProjectsCount()).isEqualTo(1);
        assertThat(summary.totalContractAmount()).isEqualTo(600_000_000L);
    }

    private Party createParty(String name) {
        return Party.create(new PartyCreateRequest(
                name,
                "대표자",
                "담당자",
                "010-1234-5678",
                "party@test.com"
        ));
    }

    private Project createProject(String code, Long partyId, ProjectStatus status, long contractAmount) {
        return Project.create(new kr.co.abacus.abms.domain.project.ProjectCreateRequest(
                partyId,
                1L,
                code,
                code,
                "테스트 프로젝트 설명",
                status,
                contractAmount,
                java.time.LocalDate.of(2024, 1, 1),
                java.time.LocalDate.of(2024, 12, 31)
        ));
    }

}
