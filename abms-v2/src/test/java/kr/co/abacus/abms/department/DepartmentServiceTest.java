package kr.co.abacus.abms.department;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

/** 부서 삭제·부서장 지정 검증은 참조하는 쪽(직원, 프로젝트)이 이벤트로 받아 막는다. */
@IntegrationTest
class DepartmentServiceTest {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private Fixtures fixtures;

    private Department root;
    private LoginUser admin;

    @BeforeEach
    void setUp() {
        root = fixtures.department("회사", null);
        admin = Fixtures.admin(fixtures.employee(root, "관리자"));
    }

    @Test
    void 소속_직원이_있는_부서는_삭제할_수_없다() {
        Department team = fixtures.department("팀", root);
        fixtures.employee(team, "팀원");

        assertThatThrownBy(() -> departmentService.delete(admin, team.id()))
                .isInstanceOf(BusinessException.class).hasMessage("소속 직원이 있는 부서는 삭제할 수 없습니다.");
        assertThat(team.isDeleted()).isFalse();
    }

    @Test
    void 소속_직원과_주관_프로젝트가_모두_있으면_소속_직원을_먼저_알린다() {
        Department team = fixtures.department("팀", root);
        fixtures.employee(team, "팀원");
        fixtures.project(team, 100_000_000, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

        assertThatThrownBy(() -> departmentService.delete(admin, team.id()))
                .isInstanceOf(BusinessException.class).hasMessage("소속 직원이 있는 부서는 삭제할 수 없습니다.");
    }

    @Test
    void 주관_프로젝트가_있는_부서는_삭제할_수_없다() {
        Department team = fixtures.department("팀", root);
        fixtures.project(team, 100_000_000, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

        assertThatThrownBy(() -> departmentService.delete(admin, team.id()))
                .isInstanceOf(BusinessException.class).hasMessage("주관 프로젝트가 있는 부서는 삭제할 수 없습니다.");
        assertThat(team.isDeleted()).isFalse();
    }

    @Test
    void 참조가_없는_부서는_삭제한다() {
        Department team = fixtures.department("팀", root);

        departmentService.delete(admin, team.id());

        assertThat(team.isDeleted()).isTrue();
    }

    @Test
    void 퇴사한_직원은_부서장으로_지정할_수_없다() {
        Employee resigned = fixtures.employee(root, "퇴사자");
        resigned.resign(LocalDate.of(2025, 1, 31));

        assertThatThrownBy(() -> departmentService.assignLeader(admin, root.id(), resigned.id()))
                .isInstanceOf(BusinessException.class).hasMessage("퇴사한 직원은 부서장으로 지정할 수 없습니다.");
    }

}
