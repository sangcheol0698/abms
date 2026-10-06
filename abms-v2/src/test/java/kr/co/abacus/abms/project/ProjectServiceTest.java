package kr.co.abacus.abms.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.access.PermissionScope;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.project.ProjectRevenuePlan.RevenuePlanInfo;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

@IntegrationTest
class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRevenueService revenueService;

    @Autowired
    private ProjectAssignmentService assignmentService;

    @Autowired
    private Fixtures fixtures;

    private final LocalDate today = LocalDate.now();
    private Department teamA;
    private Employee member;
    private Project joined;
    private Project finished;
    private Project otherTeam;

    @BeforeEach
    void setUp() {
        Department root = fixtures.department("회사", null);
        teamA = fixtures.department("A팀", root);
        Department teamB = fixtures.department("B팀", root);
        member = fixtures.employee(teamA, "참여자");

        joined = fixtures.project(teamB, 100_000_000, today.minusMonths(2), today.plusMonths(2));
        finished = fixtures.project(teamB, 100_000_000, today.minusMonths(6), today.plusMonths(2));
        otherTeam = fixtures.project(teamA, 100_000_000, today.minusMonths(1), today.plusMonths(3));
        fixtures.assign(joined, member, today.minusMonths(1), today.plusMonths(1));
        fixtures.assign(finished, member, today.minusMonths(6), today.minusMonths(3));
    }

    @Test
    void 현재_참여_범위는_진행_중인_투입_프로젝트만_보여준다() {
        LoginUser user = Fixtures.user(member, Fixtures.grants(PermissionScope.CURRENT_PARTICIPATION, PermissionCode.PROJECT_READ));

        assertThat(projectService.search(user, ProjectSearch.empty(), Pageable.unpaged())).containsExactly(joined);
        assertThatThrownBy(() -> projectService.getForRead(user, finished.id())).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void 부서_범위는_주관_부서_기준으로_적용된다() {
        LoginUser user = Fixtures.user(member, Fixtures.grants(PermissionScope.OWN_DEPARTMENT, PermissionCode.PROJECT_READ, PermissionCode.PROJECT_WRITE));

        assertThat(projectService.search(user, ProjectSearch.empty(), Pageable.unpaged())).containsExactly(otherTeam);
        assertThatThrownBy(() -> projectService.complete(user, joined.id())).isInstanceOf(AccessDeniedException.class);
        projectService.complete(user, otherTeam.id());
        assertThat(otherTeam.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
    }

    @Test
    void 매출_계획_합계는_계약금액을_넘을_수_없고_차수는_중복될_수_없다() {
        LoginUser admin = Fixtures.admin(member);
        revenueService.add(admin, joined.id(), new RevenuePlanInfo(1, today, RevenueType.DOWN_PAYMENT, Money.wons(60_000_000), null));

        assertThatThrownBy(() -> revenueService.add(admin, joined.id(),
                new RevenuePlanInfo(2, today, RevenueType.BALANCE_PAYMENT, Money.wons(50_000_000), null)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("초과");
        assertThatThrownBy(() -> revenueService.add(admin, joined.id(),
                new RevenuePlanInfo(1, today, RevenueType.BALANCE_PAYMENT, Money.wons(1_000), null)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("이미");
    }

    @Test
    void 발행된_매출은_삭제할_수_없다() {
        LoginUser admin = Fixtures.admin(member);
        ProjectRevenuePlan plan = revenueService.add(admin, joined.id(),
                new RevenuePlanInfo(1, today, RevenueType.DOWN_PAYMENT, Money.wons(10_000_000), null));
        revenueService.issue(admin, joined.id(), plan.id());

        assertThatThrownBy(() -> revenueService.delete(admin, joined.id(), plan.id())).isInstanceOf(BusinessException.class);
        revenueService.cancelIssue(admin, joined.id(), plan.id());
        revenueService.delete(admin, joined.id(), plan.id());
        assertThat(revenueService.plans(joined.id())).isEmpty();
    }

    @Test
    void 같은_직원을_같은_프로젝트에_투입률_합계_100퍼센트를_넘겨_투입할_수_없다() {
        LoginUser admin = Fixtures.admin(member);

        assertThatThrownBy(() -> assignmentService.assign(admin, joined.id(), member.id(), AssignmentRole.DEV, today, null))
                .isInstanceOf(BusinessException.class).hasMessageContaining("투입률 합계가 100%를 넘습니다");
        // 기존 투입이 끝난 다음 날부터는 다시 투입할 수 있다.
        assignmentService.assign(admin, joined.id(), member.id(), AssignmentRole.PL, today.plusMonths(1).plusDays(1), today.plusMonths(2));
        assertThat(assignmentService.assignments(joined.id())).hasSize(2);
    }

    @Test
    void 같은_직원을_다른_프로젝트에도_투입률_합계_100퍼센트를_넘겨_투입할_수_없다() {
        LoginUser admin = Fixtures.admin(member);

        assertThatThrownBy(() -> assignmentService.assign(admin, otherTeam.id(), member.id(), AssignmentRole.DEV, today, today.plusMonths(2)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("투입률 합계가 100%를 넘습니다");
        // 다른 프로젝트 투입이 끝난 다음 날부터는 투입할 수 있다.
        assignmentService.assign(admin, otherTeam.id(), member.id(), AssignmentRole.DEV, today.plusMonths(1).plusDays(1), today.plusMonths(2));
        assertThat(assignmentService.assignments(otherTeam.id())).hasSize(1);
    }

    @Test
    void 투입률을_나누면_같은_기간에_여러_프로젝트에_투입할_수_있다() {
        LoginUser admin = Fixtures.admin(member);
        Employee pm = fixtures.employee(teamA, "겸임PM");

        assignmentService.assign(admin, joined.id(), pm.id(), AssignmentRole.PM, today, today.plusMonths(1), 50);
        assignmentService.assign(admin, otherTeam.id(), pm.id(), AssignmentRole.PM, today, today.plusMonths(1), 50);

        assertThat(assignmentService.assignmentsOfEmployee(pm.id())).hasSize(2);
        assertThatThrownBy(() -> assignmentService.assign(admin, otherTeam.id(), pm.id(), AssignmentRole.DEV,
                today.plusDays(5), today.plusMonths(1), 10))
                .isInstanceOf(BusinessException.class).hasMessageContaining("다른 투입 100% + 이번 투입 10%");
    }

    @Test
    void 새_투입_기간_중간에_시작하는_기존_투입까지_합쳐_투입률을_확인한다() {
        LoginUser admin = Fixtures.admin(member);
        Employee dev = fixtures.employee(teamA, "개발자");
        assignmentService.assign(admin, joined.id(), dev.id(), AssignmentRole.DEV, today.plusDays(10), today.plusMonths(1), 60);

        // 새 투입 시작일에는 다른 투입이 없지만, 10일 뒤부터 60% + 50% 가 된다.
        assertThatThrownBy(() -> assignmentService.assign(admin, otherTeam.id(), dev.id(), AssignmentRole.DEV,
                today, today.plusMonths(1), 50))
                .isInstanceOf(BusinessException.class).hasMessageContaining(today.plusDays(10) + " 기준");
        assignmentService.assign(admin, otherTeam.id(), dev.id(), AssignmentRole.DEV, today, today.plusMonths(1), 40);
    }

    @Test
    void 투입_기간을_수정해_다른_프로젝트_투입과_합계_100퍼센트를_넘게_할_수_없다() {
        LoginUser admin = Fixtures.admin(member);
        ProjectAssignment later = assignmentService.assign(admin, otherTeam.id(), member.id(), AssignmentRole.DEV,
                today.plusMonths(1).plusDays(1), today.plusMonths(2));

        assertThatThrownBy(() -> assignmentService.update(admin, otherTeam.id(), later.id(), member.id(), AssignmentRole.DEV,
                today, today.plusMonths(2), 100))
                .isInstanceOf(BusinessException.class).hasMessageContaining("투입률 합계가 100%를 넘습니다");
        // 자기 자신과의 겹침은 무시한다.
        assignmentService.update(admin, otherTeam.id(), later.id(), member.id(), AssignmentRole.PL,
                today.plusMonths(1).plusDays(1), today.plusMonths(3), 100);
    }

    @Test
    void 이미_시작된_투입이_있는_프로젝트는_삭제할_수_없다() {
        LoginUser admin = Fixtures.admin(member);

        assertThatThrownBy(() -> projectService.delete(admin, joined.id()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("이미 시작된 투입");
        // 시작 전인 투입만 있으면 함께 삭제된다.
        Employee newcomer = fixtures.employee(teamA, "신규");
        fixtures.assign(otherTeam, newcomer, today.plusDays(1), today.plusMonths(1));
        projectService.delete(admin, otherTeam.id());
        assertThat(assignmentService.assignments(otherTeam.id())).isEmpty();
    }

    @Test
    void 직접비가_있는_프로젝트는_삭제할_수_없다() {
        LoginUser admin = Fixtures.admin(member);
        fixtures.expense(otherTeam, today, 1_000_000);

        assertThatThrownBy(() -> projectService.delete(admin, otherTeam.id()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("직접비");
    }

    @Test
    void 발행된_매출이_있는_프로젝트는_삭제할_수_없다() {
        LoginUser admin = Fixtures.admin(member);
        fixtures.revenue(otherTeam, 1, today, 10_000_000, true);

        assertThatThrownBy(() -> projectService.delete(admin, otherTeam.id()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("발행된 매출");
    }

}
