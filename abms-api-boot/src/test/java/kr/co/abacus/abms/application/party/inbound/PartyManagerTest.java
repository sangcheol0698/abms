package kr.co.abacus.abms.application.party.inbound;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.party.PartyCreateRequest;
import kr.co.abacus.abms.domain.party.PartyNotFoundException;
import kr.co.abacus.abms.domain.party.PartyUpdateRequest;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("협력사 관리 (PartyManager)")
class PartyManagerTest extends IntegrationTestBase {

    @Autowired
    private PartyManager partyManager;

    @Autowired
    private PartyRepository partyRepository;

    @Test
    @DisplayName("신규 협력사를 등록한다")
    void create() {
        PartyCreateRequest request = createRequest("신규 협력사", "김대표", "이담당", "010-1111-2222", "party@test.com");

        Party created = partyManager.create(request);
        flushAndClear();

        Party found = partyRepository.findByIdAndDeletedFalse(created.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("신규 협력사");
        assertThat(found.getCeoName()).isEqualTo("김대표");
        assertThat(found.getSalesRepName()).isEqualTo("이담당");
        assertThat(found.getSalesRepPhone()).isEqualTo("010-1111-2222");
        assertThat(found.getSalesRepEmail()).isEqualTo("party@test.com");
    }

    @Test
    @DisplayName("기존 협력사 정보를 수정한다")
    void update() {
        Party saved = partyRepository.save(createParty("수정 전 협력사"));
        flushAndClear();

        partyManager.update(saved.getId(), updateRequest("수정 후 협력사", "박대표", "최담당", "010-3333-4444", "updated@test.com"));
        flushAndClear();

        Party updated = partyRepository.findByIdAndDeletedFalse(saved.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("수정 후 협력사");
        assertThat(updated.getCeoName()).isEqualTo("박대표");
        assertThat(updated.getSalesRepName()).isEqualTo("최담당");
        assertThat(updated.getSalesRepPhone()).isEqualTo("010-3333-4444");
        assertThat(updated.getSalesRepEmail()).isEqualTo("updated@test.com");
    }

    @Test
    @DisplayName("협력사를 삭제하면 재조회할 수 없다")
    void delete() {
        Party saved = partyRepository.save(createParty("삭제 대상 협력사"));
        flushAndClear();

        partyManager.delete(saved.getId());
        flushAndClear();

        Party deleted = partyRepository.findById(saved.getId()).orElseThrow();
        assertThat(deleted.isDeleted()).isTrue();
        assertThatThrownBy(() -> partyManager.findById(saved.getId()))
                .isInstanceOf(PartyNotFoundException.class)
                .hasMessage("협력사를 찾을 수 없습니다: " + saved.getId());
    }

    @Test
    @DisplayName("존재하지 않는 협력사를 조회하면 예외가 발생한다")
    void findById_notFound() {
        assertThatThrownBy(() -> partyManager.findById(9999L))
                .isInstanceOf(PartyNotFoundException.class)
                .hasMessage("협력사를 찾을 수 없습니다: 9999");
    }

    @Test
    @DisplayName("존재하지 않는 협력사를 수정하면 예외가 발생한다")
    void update_notFound() {
        assertThatThrownBy(() -> partyManager.update(9999L,
                updateRequest("없는 협력사", "대표", "담당", "010-0000-0000", "missing@test.com")))
                .isInstanceOf(PartyNotFoundException.class)
                .hasMessage("협력사를 찾을 수 없습니다: 9999");
    }

    @Test
    @DisplayName("존재하지 않는 협력사를 삭제하면 예외가 발생한다")
    void delete_notFound() {
        assertThatThrownBy(() -> partyManager.delete(9999L))
                .isInstanceOf(PartyNotFoundException.class)
                .hasMessage("협력사를 찾을 수 없습니다: 9999");
    }

    private Party createParty(String name) {
        return Party.create(createRequest(name, "대표자", "담당자", "010-1234-5678", "party@test.com"));
    }

    private PartyCreateRequest createRequest(String name, String ceoName, String salesRepName, String salesRepPhone,
                                             String salesRepEmail) {
        return new PartyCreateRequest(name, ceoName, salesRepName, salesRepPhone, salesRepEmail);
    }

    private PartyUpdateRequest updateRequest(String name, String ceoName, String salesRepName, String salesRepPhone,
                                             String salesRepEmail) {
        return new PartyUpdateRequest(name, ceoName, salesRepName, salesRepPhone, salesRepEmail);
    }

}
