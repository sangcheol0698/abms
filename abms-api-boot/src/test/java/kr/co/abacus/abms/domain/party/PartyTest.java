package kr.co.abacus.abms.domain.party;

import kr.co.abacus.abms.support.IntegrationTestBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("협력사 도메인 테스트")
class PartyTest extends IntegrationTestBase {

    @Autowired
    private kr.co.abacus.abms.application.party.outbound.PartyRepository partyRepository;

    @Test
    @DisplayName("협력사 이름 중복 시 저장 실패")
    void createWithDuplicateName_throwsException() {
        // Given
        String name = "중복 협력사";
        Party party1 = Party.create(new PartyCreateRequest(name, null, null, null, null));
        partyRepository.save(party1);
        flushAndClear();

        // When & Then
        Party party2 = Party.create(new PartyCreateRequest(name, null, null, null, null));
        assertThatThrownBy(() -> {
            partyRepository.save(party2);
            flushAndClear();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

}
