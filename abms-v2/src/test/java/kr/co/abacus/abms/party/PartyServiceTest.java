package kr.co.abacus.abms.party;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

@IntegrationTest
class PartyServiceTest {

    @Autowired
    private PartyService partyService;

    @Autowired
    private Fixtures fixtures;

    @Test
    void 프로젝트가_연결된_협력사는_삭제할_수_없다() {
        Department root = fixtures.department("회사", null);
        Project project = fixtures.project(root, 100_000_000, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
        Party linked = partyService.get(project.getPartyId());
        Party unlinked = fixtures.party("거래 없는 협력사");

        assertThatThrownBy(() -> partyService.delete(linked.id(), 1L))
                .isInstanceOf(BusinessException.class).hasMessage("프로젝트가 연결된 협력사는 삭제할 수 없습니다.");
        assertThat(linked.isDeleted()).isFalse();

        partyService.delete(unlinked.id(), 1L);
        assertThat(unlinked.isDeleted()).isTrue();
    }

}
