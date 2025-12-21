package kr.co.abacus.abms.adapter.web.party;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;

import kr.co.abacus.abms.adapter.web.PageResponse;
import kr.co.abacus.abms.adapter.web.party.dto.PartyResponse;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.party.PartyCreateRequest;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("협력사 API (PartyApi)")
class PartyApiTest extends ApiIntegrationTestBase {

    @Autowired
    private PartyRepository partyRepository;

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

}
