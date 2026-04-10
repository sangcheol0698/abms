package kr.co.abacus.abms.adapter.api.dashboard;

import static org.springframework.restdocs.payload.PayloadDocumentation.responseBody;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.adapter.infrastructure.summary.MonthlyRevenueSummaryRepository;
import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.party.PartyCreateRequest;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummaryCreateRequest;
import kr.co.abacus.abms.support.ApiIntegrationTestBase;

@DisplayName("대시보드 API (DashboardApi)")
class DashboardApiTest extends ApiIntegrationTestBase {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MonthlyRevenueSummaryRepository monthlyRevenueSummaryRepository;

    @Test
    @DisplayName("연도 기준 대시보드 요약 정보를 조회한다")
    void getDashboardSummary() throws Exception {
        Department department = departmentRepository.save(Department.create("TEAM-DASHBOARD-API", "대시보드 API팀", DepartmentType.TEAM, null, null));
        Party party = partyRepository.save(Party.create(new PartyCreateRequest(
                "대시보드 API 협력사",
                "대표자",
                "담당자",
                "010-1234-5678",
                "dashboard-api@test.com"
        )));

        employeeRepository.save(createEmployee(department.getIdOrThrow(), "dashboard-2026@abms.co", LocalDate.of(2026, 3, 10), EmployeeType.FULL_TIME));
        employeeRepository.save(createEmployee(department.getIdOrThrow(), "dashboard-2024@abms.co", LocalDate.of(2024, 5, 1), EmployeeType.FREELANCER));

        projectRepository.save(createProject("PRJ-DASH-API-001", party.getIdOrThrow(), department.getIdOrThrow(), ProjectStatus.IN_PROGRESS, LocalDate.of(2026, 12, 31)));
        projectRepository.save(createProject("PRJ-DASH-API-002", party.getIdOrThrow(), department.getIdOrThrow(), ProjectStatus.COMPLETED, LocalDate.of(2026, 6, 30)));

        monthlyRevenueSummaryRepository.saveAll(List.of(
                createMonthlyRevenueSummary(department, 10L, "DASH-SUM-01", "1월 프로젝트", LocalDate.of(2026, 1, 20), 100_000_000L, 60_000_000L, 40_000_000L),
                createMonthlyRevenueSummary(department, 11L, "DASH-SUM-02", "2월 프로젝트", LocalDate.of(2026, 2, 20), 110_000_000L, 70_000_000L, 40_000_000L)
        ));
        flushAndClear();

        mockMvc.perform(get("/api/dashboards/summary")
                        .param("year", "2026")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("doc-user")))
                .andDo(document("dashboard/summary", responseBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEmployeesCount").value(2))
                .andExpect(jsonPath("$.activeProjectsCount").value(1))
                .andExpect(jsonPath("$.completedProjectsCount").value(1))
                .andExpect(jsonPath("$.newEmployeesCount").value(1))
                .andExpect(jsonPath("$.yearRevenue").value(210_000_000))
                .andExpect(jsonPath("$.yearProfit").value(80_000_000));
    }

    @Test
    @DisplayName("선택 연도 월별 재무 추이를 12개월로 조회한다")
    void getMonthlyFinancials() throws Exception {
        Department department = departmentRepository.save(Department.create("TEAM-DASHBOARD-MONTHLY", "대시보드 월별팀", DepartmentType.TEAM, null, null));

        monthlyRevenueSummaryRepository.saveAll(List.of(
                createMonthlyRevenueSummary(department, 101L, "MONTH-01", "1월 프로젝트", LocalDate.of(2026, 1, 5), 120_000_000L, 80_000_000L, 40_000_000L),
                createMonthlyRevenueSummary(department, 101L, "MONTH-01", "1월 프로젝트", LocalDate.of(2026, 1, 31), 140_000_000L, 90_000_000L, 50_000_000L),
                createMonthlyRevenueSummary(department, 102L, "MONTH-02", "2월 프로젝트", LocalDate.of(2026, 2, 28), 90_000_000L, 60_000_000L, 30_000_000L)
        ));
        flushAndClear();

        mockMvc.perform(get("/api/dashboards/monthly-financials")
                        .param("year", "2026")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("monthly-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(12))
                .andExpect(jsonPath("$[0].targetMonth").value("2026-01-01"))
                .andExpect(jsonPath("$[0].revenue").value(140_000_000))
                .andExpect(jsonPath("$[0].profit").value(50_000_000))
                .andExpect(jsonPath("$[1].targetMonth").value("2026-02-01"))
                .andExpect(jsonPath("$[1].revenue").value(90_000_000))
                .andExpect(jsonPath("$[2].targetMonth").value("2026-03-01"))
                .andExpect(jsonPath("$[2].revenue").value(0));
    }

    @Test
    @DisplayName("마감 임박 프로젝트를 overdue 우선, 종료일 오름차순으로 조회한다")
    void getUpcomingDeadlines() throws Exception {
        Department department = departmentRepository.save(Department.create("TEAM-DASHBOARD-DEADLINE", "대시보드 마감팀", DepartmentType.TEAM, null, null));
        Party party = partyRepository.save(Party.create(new PartyCreateRequest(
                "대시보드 마감 협력사",
                "대표자",
                "담당자",
                "010-1111-2222",
                "dashboard-deadline@test.com"
        )));

        LocalDate today = LocalDate.now();
        Project overdue = projectRepository.save(createProject(
                "PRJ-DASH-DEADLINE-001",
                party.getIdOrThrow(),
                department.getIdOrThrow(),
                ProjectStatus.IN_PROGRESS,
                today.minusDays(2)
        ));
        Project dDay = projectRepository.save(createProject(
                "PRJ-DASH-DEADLINE-002",
                party.getIdOrThrow(),
                department.getIdOrThrow(),
                ProjectStatus.ON_HOLD,
                today
        ));
        Project upcoming = projectRepository.save(createProject(
                "PRJ-DASH-DEADLINE-003",
                party.getIdOrThrow(),
                department.getIdOrThrow(),
                ProjectStatus.IN_PROGRESS,
                today.plusDays(5)
        ));
        projectRepository.save(createProject(
                "PRJ-DASH-DEADLINE-004",
                party.getIdOrThrow(),
                department.getIdOrThrow(),
                ProjectStatus.IN_PROGRESS,
                today.plusDays(45)
        ));
        projectRepository.save(createProject(
                "PRJ-DASH-DEADLINE-005",
                party.getIdOrThrow(),
                department.getIdOrThrow(),
                ProjectStatus.COMPLETED,
                today.plusDays(3)
        ));
        flushAndClear();

        mockMvc.perform(get("/api/dashboards/upcoming-deadlines")
                        .param("limit", "5")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("deadline-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectId").value(overdue.getIdOrThrow()))
                .andExpect(jsonPath("$[0].daysLeft").value(-2))
                .andExpect(jsonPath("$[1].projectId").value(dDay.getIdOrThrow()))
                .andExpect(jsonPath("$[1].daysLeft").value(0))
                .andExpect(jsonPath("$[2].projectId").value(upcoming.getIdOrThrow()))
                .andExpect(jsonPath("$[2].daysLeft").value(5))
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @DisplayName("부서별 재무 현황을 연 누적 이익 내림차순으로 조회한다")
    void getDepartmentFinancials() throws Exception {
        Department alpha = departmentRepository.save(Department.create("TEAM-DASH-ALPHA", "알파팀", DepartmentType.TEAM, null, null));
        Department beta = departmentRepository.save(Department.create("TEAM-DASH-BETA", "베타팀", DepartmentType.TEAM, null, null));

        monthlyRevenueSummaryRepository.saveAll(List.of(
                createMonthlyRevenueSummary(alpha, 101L, "ALPHA-01", "알파 프로젝트 1", LocalDate.of(2026, 1, 10), 120_000_000L, 80_000_000L, 40_000_000L),
                createMonthlyRevenueSummary(alpha, 101L, "ALPHA-01", "알파 프로젝트 1", LocalDate.of(2026, 1, 31), 140_000_000L, 90_000_000L, 50_000_000L),
                createMonthlyRevenueSummary(alpha, 102L, "ALPHA-02", "알파 프로젝트 2", LocalDate.of(2026, 2, 25), 80_000_000L, 55_000_000L, 25_000_000L),
                createMonthlyRevenueSummary(beta, 201L, "BETA-01", "베타 프로젝트 1", LocalDate.of(2026, 3, 28), 90_000_000L, 60_000_000L, 30_000_000L),
                createMonthlyRevenueSummary(beta, 202L, "BETA-02", "베타 프로젝트 2", LocalDate.of(2026, 4, 20), 0L, 0L, 0L)
        ));
        flushAndClear();

        mockMvc.perform(get("/api/dashboards/department-financials")
                        .param("year", "2026")
                        .param("limit", "5")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("financial-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].departmentName").value("알파팀"))
                .andExpect(jsonPath("$[0].revenue").value(220_000_000))
                .andExpect(jsonPath("$[0].profit").value(75_000_000))
                .andExpect(jsonPath("$[0].profitMargin").value(34.1D))
                .andExpect(jsonPath("$[1].departmentName").value("베타팀"))
                .andExpect(jsonPath("$[1].revenue").value(90_000_000))
                .andExpect(jsonPath("$[1].profit").value(30_000_000))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("선택 연도 기준 프로젝트 상태 현황을 조회한다")
    void getProjectOverview() throws Exception {
        Department department = departmentRepository.save(Department.create("TEAM-DASHBOARD-PROJECT", "대시보드 프로젝트팀", DepartmentType.TEAM, null, null));
        Party party = partyRepository.save(Party.create(new PartyCreateRequest(
                "대시보드 프로젝트 협력사",
                "대표자",
                "담당자",
                "010-3333-4444",
                "dashboard-project@test.com"
        )));

        projectRepository.save(createProject("PRJ-DASH-OV-001", party.getIdOrThrow(), department.getIdOrThrow(), ProjectStatus.SCHEDULED, LocalDate.of(2026, 12, 31)));
        projectRepository.save(createProject("PRJ-DASH-OV-002", party.getIdOrThrow(), department.getIdOrThrow(), ProjectStatus.IN_PROGRESS, LocalDate.of(2026, 11, 30)));
        projectRepository.save(createProject("PRJ-DASH-OV-003", party.getIdOrThrow(), department.getIdOrThrow(), ProjectStatus.COMPLETED, LocalDate.of(2026, 6, 30)));
        projectRepository.save(createProject("PRJ-DASH-OV-004", party.getIdOrThrow(), department.getIdOrThrow(), ProjectStatus.ON_HOLD, LocalDate.of(2026, 9, 30)));
        projectRepository.save(createProject("PRJ-DASH-OV-005", party.getIdOrThrow(), department.getIdOrThrow(), ProjectStatus.CANCELLED, LocalDate.of(2026, 3, 31)));
        projectRepository.save(createProject("PRJ-DASH-OV-006", party.getIdOrThrow(), department.getIdOrThrow(), ProjectStatus.COMPLETED, LocalDate.of(2025, 12, 31)));
        flushAndClear();

        mockMvc.perform(get("/api/dashboards/project-overview")
                        .param("year", "2026")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("project-overview-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(5))
                .andExpect(jsonPath("$.scheduledCount").value(1))
                .andExpect(jsonPath("$.inProgressCount").value(1))
                .andExpect(jsonPath("$.completedCount").value(1))
                .andExpect(jsonPath("$.onHoldCount").value(1))
                .andExpect(jsonPath("$.cancelledCount").value(1));
    }

    @Test
    @DisplayName("선택 연도 말 기준 직원 구성 현황을 조회한다")
    void getEmployeeOverview() throws Exception {
        Department department = departmentRepository.save(Department.create("TEAM-DASHBOARD-EMPLOYEE", "대시보드 직원팀", DepartmentType.TEAM, null, null));

        employeeRepository.save(createEmployee(department.getIdOrThrow(), "full-time@abms.co", LocalDate.of(2025, 1, 10), EmployeeType.FULL_TIME));
        employeeRepository.save(createEmployee(department.getIdOrThrow(), "freelancer@abms.co", LocalDate.of(2026, 3, 10), EmployeeType.FREELANCER));
        employeeRepository.save(createEmployee(department.getIdOrThrow(), "outsource@abms.co", LocalDate.of(2024, 7, 1), EmployeeType.OUTSOURCING));

        Employee partTimeResigned = employeeRepository.save(createEmployee(department.getIdOrThrow(), "part-time@abms.co", LocalDate.of(2026, 1, 5), EmployeeType.PART_TIME));
        partTimeResigned.resign(LocalDate.of(2026, 11, 1));

        Employee futureResigned = employeeRepository.save(createEmployee(department.getIdOrThrow(), "future-resigned@abms.co", LocalDate.of(2025, 8, 15), EmployeeType.FULL_TIME));
        futureResigned.resign(LocalDate.of(2027, 1, 2));
        flushAndClear();

        mockMvc.perform(get("/api/dashboards/employee-overview")
                        .param("year", "2026")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("employee-overview-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(4))
                .andExpect(jsonPath("$.fullTimeCount").value(2))
                .andExpect(jsonPath("$.freelancerCount").value(1))
                .andExpect(jsonPath("$.outsourcingCount").value(1))
                .andExpect(jsonPath("$.partTimeCount").value(0));
    }

    private Employee createEmployee(Long departmentId, String email, LocalDate joinDate, EmployeeType type) {
        return Employee.create(
                departmentId,
                "홍길동",
                email,
                joinDate,
                LocalDate.of(1990, 1, 1),
                EmployeePosition.ASSOCIATE,
                type,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        );
    }

    private Project createProject(String code, Long partyId, Long leadDepartmentId, ProjectStatus status, LocalDate endDate) {
        return Project.create(
                partyId,
                leadDepartmentId,
                code,
                code,
                "대시보드 API 프로젝트",
                status,
                100_000_000L,
                endDate.minusMonths(1),
                endDate
        );
    }

    private MonthlyRevenueSummary createMonthlyRevenueSummary(
            Department department,
            Long projectId,
            String projectCode,
            String projectName,
            LocalDate summaryDate,
            long revenue,
            long cost,
            long profit
    ) {
        return MonthlyRevenueSummary.create(new MonthlyRevenueSummaryCreateRequest(
                projectId,
                projectCode,
                projectName,
                department.getIdOrThrow(),
                department.getCode(),
                department.getName(),
                summaryDate,
                Money.wons(revenue),
                Money.wons(cost),
                Money.wons(profit)
        ));
    }
}
