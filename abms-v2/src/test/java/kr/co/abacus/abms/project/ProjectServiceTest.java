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
    void 같은_직원을_같은_프로젝트에_겹치는_기간으로_투입할_수_없다() {
        LoginUser admin = Fixtures.admin(member);

        assertThatThrownBy(() -> assignmentService.assign(admin, joined.id(), member.id(), AssignmentRole.DEV, today, null))
                .isInstanceOf(BusinessException.class).hasMessageContaining("이미 같은 기간");
        // 기존 투입이 끝난 다음 날부터는 다시 투입할 수 있다.
        assignmentService.assign(admin, joined.id(), member.id(), AssignmentRole.PL, today.plusMonths(1).plusDays(1), today.plusMonths(2));
        assertThat(assignmentService.assignments(joined.id())).hasSize(2);
    }

    @Test
    void 같은_직원을_다른_프로젝트에도_겹치는_기간으로_투입할_수_없다() {
        LoginUser admin = Fixtures.admin(member);

        assertThatThrownBy(() -> assignmentService.assign(admin, otherTeam.id(), member.id(), AssignmentRole.DEV, today, today.plusMonths(2)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("이미 같은 기간");
        // 다른 프로젝트 투입이 끝난 다음 날부터는 투입할 수 있다.
        assignmentService.assign(admin, otherTeam.id(), member.id(), AssignmentRole.DEV, today.plusMonths(1).plusDays(1), today.plusMonths(2));
        assertThat(assignmentService.assignments(otherTeam.id())).hasSize(1);
    }

    @Test
    void 투입_기간을_수정해_다른_프로젝트_투입과_겹치게_할_수_없다() {
        LoginUser admin = Fixtures.admin(member);
        ProjectAssignment later = assignmentService.assign(admin, otherTeam.id(), member.id(), AssignmentRole.DEV,
                today.plusMonths(1).plusDays(1), today.plusMonths(2));

        assertThatThrownBy(() -> assignmentService.update(admin, otherTeam.id(), later.id(), member.id(), AssignmentRole.DEV,
                today, today.plusMonths(2)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("이미 같은 기간");
        // 자기 자신과의 겹침은 무시한다.
        assignmentService.update(admin, otherTeam.id(), later.id(), member.id(), AssignmentRole.PL,
                today.plusMonths(1).plusDays(1), today.plusMonths(3));
    }

}
