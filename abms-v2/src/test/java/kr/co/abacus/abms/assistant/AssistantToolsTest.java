package kr.co.abacus.abms.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.access.PermissionCode;
import kr.co.abacus.abms.access.PermissionScope;
import kr.co.abacus.abms.department.Department;
import kr.co.abacus.abms.department.DepartmentService;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeService;
import kr.co.abacus.abms.party.PartyService;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.project.ProjectAssignmentService;
import kr.co.abacus.abms.project.ProjectRevenueService;
import kr.co.abacus.abms.project.ProjectService;
import kr.co.abacus.abms.security.LoginUser;
import kr.co.abacus.abms.summary.ProfitQueryService;
import kr.co.abacus.abms.support.Fixtures;
import kr.co.abacus.abms.support.IntegrationTest;

/**
 * AI 어시스턴트 도구는 질문한 사용자의 권한 범위 안에서만 데이터를 돌려줘야 한다.
 */
@IntegrationTest
class AssistantToolsTest {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectRevenueService revenueService;
    @Autowired
    private ProjectAssignmentService assignmentService;
    @Autowired
    private PartyService partyService;
    @Autowired
    private ProfitQueryService profitQueryService;
    @Autowired
    private Fixtures fixtures;

    private Employee member;
    private Employee colleague;
    private Project mine;
    private Project others;

    @BeforeEach
    void setUp() {
        Department team = fixtures.department("AI팀", null);
        Department otherTeam = fixtures.department("다른팀", null);
        member = fixtures.employee(team, "질문자");
        colleague = fixtures.employee(otherTeam, "동료");
        LocalDate today = LocalDate.now();
        mine = fixtures.project(team, 100_000_000, today.minusMonths(1), today.plusMonths(1));
        others = fixtures.project(otherTeam, 100_000_000, today.minusMonths(1), today.plusMonths(1));
        fixtures.assign(mine, member, today.minusMonths(1), today.plusMonths(1));
    }

    @Test
    void 참여_프로젝트만_검색되고_다른_프로젝트_상세는_거절한다() {
        AssistantTools tools = toolsFor(Fixtures.user(member, Fixtures.grants(PermissionScope.CURRENT_PARTICIPATION, PermissionCode.PROJECT_READ)));

        @SuppressWarnings("unchecked")
        List<AssistantTools.ProjectItem> projects = (List<AssistantTools.ProjectItem>) tools.searchProjects(null, "진행 중");
        assertThat(projects).extracting(AssistantTools.ProjectItem::id).containsExactly(mine.id());
        assertThat(tools.getProjectDetail(mine.id())).isInstanceOf(AssistantTools.ProjectDetail.class);
        assertThat(tools.getProjectDetail(others.id())).asString().contains("권한이 없습니다");
    }

    @Test
    void 권한이_없으면_안내_메시지를_돌려준다() {
        AssistantTools tools = toolsFor(Fixtures.user(member, java.util.Map.of()));

        assertThat(tools.searchProjects(null, null)).asString().contains("권한이 없습니다");
        assertThat(tools.getMonthlyProfit("2026-01")).asString().contains("권한이 없습니다");
        assertThat(tools.getEmployeeDetail(colleague.id())).asString().contains("권한이 없습니다");
        assertThat(tools.searchEmployees("동료")).extracting(AssistantTools.EmployeeItem::name).containsExactly("동료");
    }

    private AssistantTools toolsFor(LoginUser user) {
        return new AssistantTools(user, employeeService, departmentService, projectService, revenueService,
                assignmentService, partyService, profitQueryService);
    }

}
