package kr.co.abacus.abms.domain.party;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("협력사 도메인 테스트")
class PartyTest {

    @Test
    @DisplayName("협력사 입력값을 정규화한다")
    void create_normalizesInput() {
        Party party = Party.create(new PartyCreateRequest(
                "  협력사  ",
                "  대표  ",
                "  ",
                null,
                "  party@test.com  "
        ));

        assertThat(party.getName()).isEqualTo("협력사");
        assertThat(party.getCeoName()).isEqualTo("대표");
        assertThat(party.getSalesRepName()).isNull();
        assertThat(party.getSalesRepEmail()).isEqualTo("party@test.com");
    }

    @Test
    @DisplayName("협력사명은 공백일 수 없다")
    void create_blankName() {
        assertThatThrownBy(() -> Party.create(new PartyCreateRequest(" ", null, null, null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("협력사명은 필수입니다.");
    }

}
