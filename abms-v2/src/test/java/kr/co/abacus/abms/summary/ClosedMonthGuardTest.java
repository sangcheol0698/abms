package kr.co.abacus.abms.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeService;
import kr.co.abacus.abms.employee.EmployeeType;
import kr.co.abacus.abms.project.AssignmentRole;
import kr.co.abacus.abms.project.ExpenseCategory;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.project.ProjectAssignment;
import kr.co.abacus.abms.project.ProjectAssignmentService;
import kr.co.abacus.abms.project.ProjectExpense;
import kr.co.abacus.abms.project.ProjectExpense.ExpenseInfo;
import kr.co.abacus.abms.project.ProjectExpenseService;
import kr.co.abacus.abms.project.ProjectRevenuePlan;
import kr.co.abacus.abms.project.ProjectRevenuePlan.RevenuePlanInfo;
import kr.co.abacus.abms.project.ProjectRevenueService;
import kr.co.abacus.abms.project.RevenueType;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

/**
 * 마감된 월(2026-02)의 손익에 영향을 주는 원천 데이터 변경은 막는다.
 */
@IntegrationTest
class ClosedMonthGuardTest {

    private static final YearMonth FEB = YearMonth.of(2026, 2);

    @Autowired
    private MonthClosingService closingService;

    @Autowired
    private ProjectRevenueService revenueService;

    @Autowired
    private ProjectAssignmentService assignmentService;

    @Autowired
    private ProjectExpenseService expenseService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private CostPolicyService costPolicyService;

    @Autowired
    private EmployeeCostPolicyRepository policyRepository;

    @Autowired
    private Fixtures fixtures;

    private LoginUser admin;
    private Employee member;
    private Project project;

    @BeforeEach
    void setUp() {
        Department team = fixtures.department("주관팀", null);
        member = fixtures.employee(team, "팀원");
        fixtures.payroll(member, 120_000_000, LocalDate.of(2025, 1, 1));
        project = fixtures.project(team, 1_000_000_000, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31));
        admin = Fixtures.admin(member);
    }

    @Test
    void 마감_월의_세금계산서는_발행하거나_취소할_수_없다() {
        ProjectRevenuePlan unissued = fixtures.revenue(project, 1, LocalDate.of(2026, 2, 10), 100_000_000, false);
        ProjectRevenuePlan issued = fixtures.revenue(project, 2, LocalDate.of(2026, 2, 20), 100_000_000, true);
        ProjectRevenuePlan march = fixtures.revenue(project, 3, LocalDate.of(2026, 3, 10), 100_000_000, false);
        closingService.close(admin, FEB);

        assertThatThrownBy(() -> revenueService.issue(admin, project.id(), unissued.id()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("2026-02");
        assertThatThrownBy(() -> revenueService.cancelIssue(admin, project.id(), issued.id()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("마감된 월");
        revenueService.issue(admin, project.id(), march.id());
        assertThat(march.isIssued()).isTrue();
    }

    @Test
    void 마감_월의_발행된_매출은_청구일과_금액을_바꿀_수_없다() {
        ProjectRevenuePlan issued = fixtures.revenue(project, 1, LocalDate.of(2026, 2, 20), 100_000_000, true);
        ProjectRevenuePlan march = fixtures.revenue(project, 2, LocalDate.of(2026, 3, 10), 100_000_000, true);
        closingService.close(admin, FEB);

        assertThatThrownBy(() -> revenueService.update(admin, project.id(), issued.id(),
                new RevenuePlanInfo(1, LocalDate.of(2026, 3, 5), RevenueType.DOWN_PAYMENT, Money.wons(100_000_000), null)))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> revenueService.update(admin, project.id(), march.id(),
                new RevenuePlanInfo(2, LocalDate.of(2026, 2, 25), RevenueType.DOWN_PAYMENT, Money.wons(100_000_000), null)))
                .isInstanceOf(BusinessException.class);
        // 메모만 바꾸는 것은 집계에 영향이 없으므로 허용한다.
        revenueService.update(admin, project.id(), issued.id(),
                new RevenuePlanInfo(1, LocalDate.of(2026, 2, 20), RevenueType.DOWN_PAYMENT, Money.wons(100_000_000), "메모"));
        assertThat(issued.getMemo()).isEqualTo("메모");
    }

    @Test
    void 마감_월의_직접비는_등록_삭제하거나_금액_귀속일을_바꿀_수_없다() {
        ProjectExpense feb = fixtures.expense(project, LocalDate.of(2026, 2, 10), 1_000_000);
        ProjectExpense march = fixtures.expense(project, LocalDate.of(2026, 3, 10), 1_000_000);
        closingService.close(admin, FEB);

        assertThatThrownBy(() -> expenseService.add(admin, project.id(),
                new ExpenseInfo(LocalDate.of(2026, 2, 20), ExpenseCategory.TRAVEL, Money.wons(500_000), "출장", null)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("직접비 등록");
        assertThatThrownBy(() -> expenseService.update(admin, project.id(), feb.id(),
                new ExpenseInfo(LocalDate.of(2026, 2, 10), ExpenseCategory.OUTSOURCING, Money.wons(2_000_000), "외주 용역", null)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("직접비 수정");
        assertThatThrownBy(() -> expenseService.update(admin, project.id(), march.id(),
                new ExpenseInfo(LocalDate.of(2026, 2, 28), ExpenseCategory.OUTSOURCING, Money.wons(1_000_000), "외주 용역", null)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("직접비 수정");
        assertThatThrownBy(() -> expenseService.delete(admin, project.id(), feb.id()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("직접비 삭제");
        // 분류·내용만 바꾸는 것은 집계 금액에 영향이 없으므로 허용한다.
        expenseService.update(admin, project.id(), feb.id(),
                new ExpenseInfo(LocalDate.of(2026, 2, 10), ExpenseCategory.EQUIPMENT, Money.wons(1_000_000), "장비 임차", "메모"));
        assertThat(feb.getCategory()).isEqualTo(ExpenseCategory.EQUIPMENT);
    }

    @Test
    void 마감_월에_걸친_투입의_투입률은_바꿀_수_없다() {
        ProjectAssignment assignment = assignmentService.assign(admin, project.id(), member.id(), AssignmentRole.DEV,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));
        closingService.close(admin, FEB);

        assertThatThrownBy(() -> assignmentService.update(admin, project.id(), assignment.id(), member.id(), AssignmentRole.DEV,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30), 50))
                .isInstanceOf(BusinessException.class).hasMessageContaining("투입 수정");
    }

    @Test
    void 마감_월에_걸친_투입은_마감_월의_MM이_바뀌는_변경만_막는다() {
        ProjectAssignment assignment = assignmentService.assign(admin, project.id(), member.id(), AssignmentRole.DEV,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30));
        closingService.close(admin, FEB);

        // 마감 이후 구간(종료일)과 역할 변경은 허용
        assignmentService.update(admin, project.id(), assignment.id(), member.id(), AssignmentRole.PL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 9, 30), 100);
        assertThat(assignment.getPeriod().endDate()).isEqualTo(LocalDate.of(2026, 9, 30));

        assertThatThrownBy(() -> assignmentService.update(admin, project.id(), assignment.id(), member.id(), AssignmentRole.PL,
                LocalDate.of(2026, 2, 15), LocalDate.of(2026, 9, 30), 100))
                .isInstanceOf(BusinessException.class).hasMessageContaining("투입 수정");
        assertThatThrownBy(() -> assignmentService.update(admin, project.id(), assignment.id(), member.id(), AssignmentRole.PL,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), 100))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> assignmentService.delete(admin, project.id(), assignment.id()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("투입 삭제");
    }

    @Test
    void 마감_월에_새로_투입할_수_없다() {
        closingService.close(admin, FEB);

        assertThatThrownBy(() -> assignmentService.assign(admin, project.id(), member.id(), AssignmentRole.DEV,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 6, 30)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("투입 등록");
        assertThatCode(() -> assignmentService.assign(admin, project.id(), member.id(), AssignmentRole.DEV,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30))).doesNotThrowAnyException();
    }

    @Test
    void 마감_월에_영향을_주는_연봉_변경과_퇴사_처리는_막는다() {
        closingService.close(admin, FEB);

        assertThatThrownBy(() -> employeeService.changeSalary(admin, member.id(), Money.wons(130_000_000), LocalDate.of(2026, 2, 1)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("연봉 변경");
        // 1월 퇴사로 처리하면 마감된 2월 원가에서 빠져야 하므로 막는다.
        assertThatThrownBy(() -> employeeService.resign(admin, member.id(), LocalDate.of(2026, 1, 15)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("퇴사");

        employeeService.changeSalary(admin, member.id(), Money.wons(130_000_000), LocalDate.of(2026, 3, 1));
        // 2월 퇴사는 2월 원가에 그대로 포함되므로 허용한다.
        employeeService.resign(admin, member.id(), LocalDate.of(2026, 2, 20));
        assertThat(member.isResigned()).isTrue();
    }

    @Test
    void 마감_월이_포함된_연도의_원가_정책은_마감_해제_전까지_바꿀_수_없다() {
        EmployeeCostPolicy policy = policyRepository.findByApplyYearAndType(2026, EmployeeType.FULL_TIME).orElseThrow();
        closingService.close(admin, FEB);

        assertThatThrownBy(() -> costPolicyService.update(policy.id(), new BigDecimal("0.2"), new BigDecimal("0.1")))
                .isInstanceOf(BusinessException.class).hasMessageContaining("원가 정책 수정");

        closingService.reopen(admin, FEB);
        costPolicyService.update(policy.id(), new BigDecimal("0.2"), new BigDecimal("0.1"));
        assertThat(policy.getOverheadRate()).isEqualByComparingTo("0.2");
    }

    @Test
    void 마감_월에_재직한_직원은_삭제하거나_복구할_수_없다() {
        Employee newcomer = fixtures.employee(fixtures.department("다른팀", null), "신규");
        fixtures.payroll(newcomer, 60_000_000, LocalDate.of(2025, 1, 1));
        employeeService.delete(admin, newcomer.id());
        closingService.close(admin, FEB);

        assertThatThrownBy(() -> employeeService.restore(admin, newcomer.id()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("직원 복구");
        assertThatThrownBy(() -> employeeService.delete(admin, member.id()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("직원 삭제");
    }

}
