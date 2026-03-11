package kr.co.abacus.abms.adapter.api.party;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.adapter.api.common.PageResponse;
import kr.co.abacus.abms.adapter.api.party.dto.PartyResponse;
import kr.co.abacus.abms.adapter.api.project.dto.ProjectResponse;
import kr.co.abacus.abms.application.party.dto.PartyOverviewSummary;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.party.PartyCreateRequest;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectFixture;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

import org.springframework.core.ParameterizedTypeReference;

@DisplayName("협력사 API (PartyApi)")
class PartyApiTest extends ApiIntegrationTestBase {

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    @DisplayName("협력사 목록 조회 - 페이징")
    void list_withPaging() {
        // Given: 15개의 협력사 생성
        for (int i = 1; i <= 15; i++) {
            Party party = Party.create(new PartyCreateRequest(
                    "협력사 " + i,
                    "대표이사 " + i,
                    "담당자 " + i,
                    "010-0000-000" + i,
                    "contact" + i + "@example.com"));
            partyRepository.save(party);
        }
        flushAndClear();

        // When & Then: 첫 번째 페이지 (size=10)
        restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/parties")
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(10)
                .jsonPath("$.totalElements").isEqualTo(15)
                .jsonPath("$.totalPages").isEqualTo(2)
                .jsonPath("$.last").isEqualTo(false);
    }

    @Test
    @DisplayName("협력사 목록 조회 - 응답 구조 확인")
    void list_responseStructure() throws Exception {
        // Given
        Party party = Party.create(new PartyCreateRequest(
                "테스트 협력사",
                "홍길동",
                "김담당",
                "010-1234-5678",
                "contact@test.com"));
        partyRepository.save(party);
        flushAndClear();

        // When & Then
        PageResponse<PartyResponse> responses = restTestClient.get()
                .uri("/api/parties?page=0&size=10")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<PartyResponse>>() {
                })
                .returnResult()
                .getResponseBody();

        assertThat(responses.content()).hasSize(1);
        assertThat(responses.content().get(0).name()).isEqualTo("테스트 협력사");
        assertThat(responses.content().get(0).ceo()).isEqualTo("홍길동");
        assertThat(responses.content().get(0).manager()).isEqualTo("김담당");
        assertThat(responses.content().get(0).contact()).isEqualTo("010-1234-5678");
        assertThat(responses.content().get(0).email()).isEqualTo("contact@test.com");
    }

    @Test
    @DisplayName("협력사 프로젝트 조회")
    void list_projectsByParty() {
        Party party = partyRepository.save(Party.create(new PartyCreateRequest(
                "테스트 협력사",
                "홍길동",
                "김담당",
                "010-1234-5678",
                "contact@test.com")));
        Party otherParty = partyRepository.save(Party.create(new PartyCreateRequest(
                "다른 협력사",
                "임꺽정",
                "박담당",
                "010-0000-0000",
                "contact2@test.com")));

        projectRepository.save(ProjectFixture.createProject(
                "PRJ-PARTY-001", "협력사 프로젝트 1", party.getId(), 1L));
        projectRepository.save(ProjectFixture.createProject(
                "PRJ-PARTY-002", "협력사 프로젝트 2", party.getId(), 1L));
        projectRepository.save(ProjectFixture.createProject(
                "PRJ-OTHER-001", "다른 협력사 프로젝트", otherParty.getId(), 1L));
        flushAndClear();

        var response = restTestClient.get()
                .uri("/api/parties/{id}/projects", party.getId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<java.util.List<ProjectResponse>>() {
                })
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response)
                .extracting(ProjectResponse::partyId)
                .containsOnly(party.getId());
        assertThat(response)
                .extracting(ProjectResponse::code)
                .containsExactlyInAnyOrder("PRJ-PARTY-001", "PRJ-PARTY-002");
    }

    @Test
    @DisplayName("협력사 요약 정보를 조회한다")
    void summary() {
        Party alpha = partyRepository.save(Party.create(new PartyCreateRequest(
                "요약 협력사 Alpha",
                "홍길동",
                "김담당",
                "010-1234-5678",
                "contact@test.com")));
        Party beta = partyRepository.save(Party.create(new PartyCreateRequest(
                "요약 협력사 Beta",
                "홍길동",
                "김담당",
                "010-1234-5678",
                "contact@test.com")));
        partyRepository.save(Party.create(new PartyCreateRequest(
                "요약 협력사 Gamma",
                "홍길동",
                "김담당",
                "010-1234-5678",
                "contact@test.com")));

        projectRepository.save(createProject("PRJ-PARTY-SUM-API-001", alpha.getId(), ProjectStatus.IN_PROGRESS, 100_000_000L));
        projectRepository.save(createProject("PRJ-PARTY-SUM-API-002", beta.getId(), ProjectStatus.COMPLETED, 200_000_000L));
        flushAndClear();

        PartyOverviewSummary response = restTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/parties/summary")
                        .queryParam("name", "요약 협력사")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PartyOverviewSummary.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.totalCount()).isEqualTo(3);
        assertThat(response.withProjectsCount()).isEqualTo(2);
        assertThat(response.withInProgressProjectsCount()).isEqualTo(1);
        assertThat(response.withoutProjectsCount()).isEqualTo(1);
        assertThat(response.totalContractAmount()).isEqualTo(300_000_000L);
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
