package kr.co.abacus.abms.application.party.inbound;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.domain.party.DuplicatePartyNameException;
import kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.party.PartyCreateRequest;
import kr.co.abacus.abms.domain.party.PartyDeletionDeniedException;
import kr.co.abacus.abms.domain.party.PartyNotFoundException;
import kr.co.abacus.abms.domain.party.PartyUpdateRequest;
import kr.co.abacus.abms.domain.project.ProjectFixture;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("협력사 관리 (PartyManager)")
class PartyManagerTest extends IntegrationTestBase {

    @Autowired
    private PartyManager partyManager;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    @DisplayName("신규 협력사를 등록한다")
    void create() {
        PartyCreateRequest request = createRequest("신규 협력사", "김대표", "이담당", "010-1111-2222", "party@test.com");

        Party created = partyManager.create(partyWriteActor(), request);
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

        partyManager.update(
                partyWriteActor(),
                saved.getId(),
                updateRequest("수정 후 협력사", "박대표", "최담당", "010-3333-4444", "updated@test.com")
        );
        flushAndClear();

        Party updated = partyRepository.findByIdAndDeletedFalse(saved.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("수정 후 협력사");
        assertThat(updated.getCeoName()).isEqualTo("박대표");
        assertThat(updated.getSalesRepName()).isEqualTo("최담당");
        assertThat(updated.getSalesRepPhone()).isEqualTo("010-3333-4444");
        assertThat(updated.getSalesRepEmail()).isEqualTo("updated@test.com");
    }

    @Test
    @DisplayName("이미 사용 중인 협력사명으로 등록할 수 없다")
    void create_duplicateName() {
        partyRepository.save(createParty("중복 협력사"));
        flushAndClear();

        assertThatThrownBy(() -> partyManager.create(partyWriteActor(), createRequest("중복 협력사", null, null, null, null)))
                .isInstanceOf(DuplicatePartyNameException.class)
                .hasMessage("이미 존재하는 협력사명입니다: 중복 협력사");
    }

    @Test
    @DisplayName("이미 사용 중인 협력사명으로 수정할 수 없다")
    void update_duplicateName() {
        partyRepository.save(createParty("기존 협력사"));
        Party target = partyRepository.save(createParty("수정 대상 협력사"));
        flushAndClear();

        assertThatThrownBy(() -> partyManager.update(
                partyWriteActor(),
                target.getId(),
                updateRequest("기존 협력사", null, null, null, null)
        ))
                .isInstanceOf(DuplicatePartyNameException.class)
                .hasMessage("이미 존재하는 협력사명입니다: 기존 협력사");
    }

    @Test
    @DisplayName("협력사를 삭제하면 재조회할 수 없다")
    void delete() {
        Party saved = partyRepository.save(createParty("삭제 대상 협력사"));
        flushAndClear();

        partyManager.delete(partyWriteActor(), saved.getId());
        flushAndClear();

        Party deleted = partyRepository.findById(saved.getId()).orElseThrow();
        assertThat(deleted.isDeleted()).isTrue();
        assertThat(deleted.getDeletedBy()).isEqualTo(partyWriteActor().accountId());
        assertThatThrownBy(() -> partyManager.findById(saved.getId()))
                .isInstanceOf(PartyNotFoundException.class)
                .hasMessage("협력사를 찾을 수 없습니다: " + saved.getId());
    }

    @Test
    @DisplayName("삭제된 협력사명은 다시 등록할 수 있다")
    void create_afterDeleteWithSameName() {
        Party saved = partyRepository.save(createParty("재등록 협력사"));
        flushAndClear();

        partyManager.delete(partyWriteActor(), saved.getId());
        flushAndClear();

        Party created = partyManager.create(partyWriteActor(), createRequest("재등록 협력사", null, null, null, null));
        flushAndClear();

        assertThat(created.getId()).isNotEqualTo(saved.getId());
        assertThat(partyRepository.findByIdAndDeletedFalse(created.getId())).isPresent();
    }

    @Test
    @DisplayName("프로젝트가 연결된 협력사는 삭제할 수 없다")
    void delete_hasProjects() {
        Party saved = partyRepository.save(createParty("프로젝트 보유 협력사"));
        projectRepository.save(ProjectFixture.createProject(saved.getId()));
        flushAndClear();

        assertThatThrownBy(() -> partyManager.delete(partyWriteActor(), saved.getId()))
                .isInstanceOf(PartyDeletionDeniedException.class)
                .hasMessage("프로젝트가 연결된 협력사는 삭제할 수 없습니다: " + saved.getId());
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
        assertThatThrownBy(() -> partyManager.update(partyWriteActor(), 9999L,
                updateRequest("없는 협력사", "대표", "담당", "010-0000-0000", "missing@test.com")))
                .isInstanceOf(PartyNotFoundException.class)
                .hasMessage("협력사를 찾을 수 없습니다: 9999");
    }

    @Test
    @DisplayName("존재하지 않는 협력사를 삭제하면 예외가 발생한다")
    void delete_notFound() {
        assertThatThrownBy(() -> partyManager.delete(partyWriteActor(), 9999L))
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

    private CurrentActor partyWriteActor() {
        return new CurrentActor(
                1L,
                "party-manager-test",
                null,
                null,
                Map.of("party.write", Set.of(PermissionScope.ALL))
        );
    }

}
