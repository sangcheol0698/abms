package kr.co.abacus.abms.party;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Location;
import kr.co.abacus.abms.party.Party.PartyInfo;

class PartyTest {

    private static PartyInfo info(String businessNumber, String website) {
        return new PartyInfo("한빛클라우드", "이한빛", PartyType.BOTH, businessNumber, " 클라우드 ", "02-123-4567",
                Location.of(null, "서울시 중구", null, null, null), website, "  ");
    }

    @Test
    void 사업자등록번호는_000_00_00000_형식으로_맞추고_웹사이트에는_프로토콜을_붙인다() {
        Party party = Party.create(info("1234567890", "hanbit.example"));

        assertThat(party.getBusinessNumber()).isEqualTo("123-45-67890");
        assertThat(party.getWebsite()).isEqualTo("https://hanbit.example");
        assertThat(party.getPartyType()).isEqualTo(PartyType.BOTH);
        assertThat(party.getIndustry()).isEqualTo("클라우드");
        assertThat(party.getMemo()).isNull();
    }

    @Test
    void 사업자등록번호가_10자리가_아니면_거부한다() {
        assertThatThrownBy(() -> Party.create(info("123-45-678", null)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("사업자등록번호");
    }

    @Test
    void 기존_생성자로_만들면_구분은_고객사다() {
        Party party = Party.create(new PartyInfo("누리시스템즈"));

        assertThat(party.getPartyType()).isEqualTo(PartyType.CLIENT);
    }

    @Test
    void 주소가_없으면_빈_위치를_돌려준다() {
        Party party = Party.create(new PartyInfo("누리시스템즈"));

        assertThat(party.getLocation().isEmpty()).isTrue();
        assertThat(party.getLocation().hasCoordinates()).isFalse();
    }

}
