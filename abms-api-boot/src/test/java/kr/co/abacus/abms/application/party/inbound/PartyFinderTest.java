package kr.co.abacus.abms.application.party.inbound;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import kr.co.abacus.abms.application.party.PartyQueryService;
import kr.co.abacus.abms.application.party.dto.PartyListItem;
import kr.co.abacus.abms.application.party.dto.PartyOverviewSummary;
import kr.co.abacus.abms.application.party.dto.PartySearchCondition;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.party.PartyCreateRequest;
import kr.co.abacus.abms.domain.party.PartyNotFoundException;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("협력사 조회 (PartyFinder)")
class PartyFinderTest extends IntegrationTestBase {

    @Autowired
    private PartyFinder partyFinder;

    @Autowired
    private PartyQueryService partyQueryService;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    @DisplayName("이름 필터와 정렬 조건으로 협력사 목록을 조회한다")
    void getParties() {
        partyRepository.save(createParty("감마 협력사"));
        partyRepository.save(createParty("알파 협력사"));
        partyRepository.save(createParty("대상 외 협력사"));
        flushAndClear();

        Page<PartyListItem> result = partyFinder.getParties(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name")),
                new PartySearchCondition("협력사"));

        assertThat(result.getContent())
                .extracting(PartyListItem::name)
                .containsExactly("감마 협력사", "대상 외 협력사", "알파 협력사");
    }

    @Test
    @DisplayName("삭제된 협력사는 목록 조회에서 제외된다")
    void getParties_excludesDeletedParty() {
        Party active = partyRepository.save(createParty("활성 협력사"));
        Party deleted = partyRepository.save(createParty("삭제 협력사"));
        deleted.softDelete(null);
        flushAndClear();

        Page<PartyListItem> result = partyFinder.getParties(PageRequest.of(0, 10), new PartySearchCondition("협력사"));

        assertThat(result.getContent())
                .extracting(PartyListItem::partyId)
                .contains(active.getId())
                .doesNotContain(deleted.getId());
    }

    @Test
    @DisplayName("협력사 요약은 이름 필터에 맞는 프로젝트 집계만 반환한다")
    void getOverviewSummary() {
        Party alpha = partyRepository.save(createParty("요약 대상 Alpha"));
        Party beta = partyRepository.save(createParty("요약 대상 Beta"));
        Party other = partyRepository.save(createParty("집계 제외"));

        projectRepository.save(createProject("PRJ-PARTY-FINDER-001", alpha.getId(), ProjectStatus.IN_PROGRESS, 100_000_000L));
        projectRepository.save(createProject("PRJ-PARTY-FINDER-002", beta.getId(), ProjectStatus.COMPLETED, 200_000_000L));
        projectRepository.save(createProject("PRJ-PARTY-FINDER-003", other.getId(), ProjectStatus.COMPLETED, 300_000_000L));
        flushAndClear();

        PartyOverviewSummary summary = partyFinder.getOverviewSummary(new PartySearchCondition("요약 대상"));

        assertThat(summary.totalCount()).isEqualTo(2);
        assertThat(summary.withProjectsCount()).isEqualTo(2);
        assertThat(summary.withInProgressProjectsCount()).isEqualTo(1);
        assertThat(summary.withoutProjectsCount()).isZero();
        assertThat(summary.totalContractAmount()).isEqualTo(300_000_000L);
    }

    @Test
    @DisplayName("협력사 ID로 이름을 조회한다")
    void getPartyName() {
        Party saved = partyRepository.save(createParty("이름 조회 협력사"));
        flushAndClear();

        String partyName = partyQueryService.getPartyName(saved.getId());

        assertThat(partyName).isEqualTo("이름 조회 협력사");
    }

    @Test
    @DisplayName("존재하지 않는 협력사의 이름 조회 시 예외가 발생한다")
    void getPartyName_notFound() {
        assertThatThrownBy(() -> partyQueryService.getPartyName(9999L))
                .isInstanceOf(PartyNotFoundException.class)
                .hasMessage("존재하지 않는 협력사입니다: 9999");
    }

    @Test
    @DisplayName("삭제된 협력사의 이름 조회 시 예외가 발생한다")
    void getPartyName_deleted() {
        Party saved = partyRepository.save(createParty("삭제된 이름 조회 협력사"));
        saved.softDelete(1L);
        flushAndClear();

        assertThatThrownBy(() -> partyQueryService.getPartyName(saved.getId()))
                .isInstanceOf(PartyNotFoundException.class)
                .hasMessage("존재하지 않는 협력사입니다: " + saved.getId());
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
        return Project.create(
                partyId,
                1L,
                code,
                code,
                "테스트 프로젝트 설명",
                status,
                contractAmount,
                java.time.LocalDate.of(2024, 1, 1),
                java.time.LocalDate.of(2024, 12, 31)
        );
    }

}
