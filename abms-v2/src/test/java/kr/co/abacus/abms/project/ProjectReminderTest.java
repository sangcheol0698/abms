package kr.co.abacus.abms.project;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import kr.co.abacus.abms.account.Account;
import kr.co.abacus.abms.account.AccountRepository;
import kr.co.abacus.abms.common.domain.Period;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.department.DepartmentRepository;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.notification.Notification;
import kr.co.abacus.abms.notification.NotificationRepository;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

@IntegrationTest
class ProjectReminderTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 5);

    @Autowired
    private ProjectReminderService reminderService;

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ProjectAssignmentRepository assignmentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private Department team;
    private Employee leader;
    private Employee pm;
    private Employee dev;
    private Project project;

    @BeforeEach
    void setUp() {
        team = fixtures.department("알림팀", null);
        leader = withAccount(fixtures.employee(team, "부서장"));
        pm = withAccount(fixtures.employee(team, "피엠"));
        dev = withAccount(fixtures.employee(team, "개발자"));
        team.assignLeader(leader.id());
        departmentRepository.save(team);
        project = fixtures.project(team, 300_000_000, TODAY.minusMonths(3), TODAY.plusMonths(6));
        assignmentRepository.save(ProjectAssignment.assign(project, pm, AssignmentRole.PM, new Period(TODAY.minusMonths(3), TODAY.plusMonths(6))));
    }

    private Employee withAccount(Employee employee) {
        accountRepository.save(Account.create(employee.id(), "r" + employee.id() + "@test.co", "{noop}x"));
        return employee;
    }

    private List<String> titlesOf(Employee employee) {
        Long accountId = accountRepository.findByEmployeeId(employee.id()).orElseThrow().id();
        return notificationRepository.findAllByAccountIdOrderByCreatedAtDesc(accountId, PageRequest.of(0, 50)).stream().map(Notification::getTitle).toList();
    }

    @Test
    void 청구일_3일_전에_주관_부서장과_PM에게_알리고_같은_날_다시_돌아도_한_번만_보낸다() {
        fixtures.revenue(project, 1, TODAY.plusDays(3), 50_000_000, false);
        fixtures.revenue(project, 2, TODAY.plusDays(3).plusDays(1), 50_000_000, false);

        reminderService.run(TODAY);
        reminderService.run(TODAY);

        assertThat(titlesOf(leader)).hasSize(1).first().asString().startsWith("청구일 3일 전").contains("1차");
        assertThat(titlesOf(pm)).hasSize(1);
        assertThat(titlesOf(dev)).isEmpty();
    }

    @Test
    void 청구_기한이_지나면_다음_날과_이후_7일마다_알리고_발행한_매출은_알리지_않는다() {
        fixtures.revenue(project, 1, TODAY.minusDays(1), 10_000_000, false);
        fixtures.revenue(project, 2, TODAY.minusDays(8), 10_000_000, false);
        fixtures.revenue(project, 3, TODAY.minusDays(2), 10_000_000, false);
        fixtures.revenue(project, 4, TODAY.minusDays(1), 10_000_000, true);

        reminderService.run(TODAY);

        assertThat(titlesOf(leader)).extracting(t -> t.substring(0, t.indexOf(':')))
                .containsExactlyInAnyOrder("미발행 매출 1일 경과", "미발행 매출 8일 경과");
    }

    @Test
    void 프로젝트와_투입이_14일_뒤_끝나면_알린다() {
        Project ending = fixtures.project(team, 100_000_000, TODAY.minusMonths(2), TODAY.plusDays(14));
        assignmentRepository.save(ProjectAssignment.assign(ending, dev, AssignmentRole.DEV, new Period(TODAY.minusMonths(1), TODAY.plusDays(14))));

        reminderService.run(TODAY);

        assertThat(titlesOf(leader)).anyMatch(t -> t.startsWith("프로젝트 종료 14일 전")).anyMatch(t -> t.startsWith("투입 종료 14일 전: 개발자"));
        assertThat(titlesOf(dev)).anyMatch(t -> t.startsWith("투입 종료 14일 전"));
    }

}
