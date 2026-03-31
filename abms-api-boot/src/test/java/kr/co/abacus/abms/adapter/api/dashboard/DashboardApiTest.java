package kr.co.abacus.abms.adapter.api.dashboard;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kr.co.abacus.abms.application.dashboard.dto.DashboardSummaryResponse;
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

    @Test
    @DisplayName("대시보드 요약 정보를 조회한다")
    void getDashboardSummary() {
        Department department = departmentRepository.save(Department.create("TEAM-DASHBOARD-API", "대시보드 API팀", DepartmentType.TEAM, null, null));
        Party party = partyRepository.save(Party.create(new PartyCreateRequest(
                "대시보드 API 협력사",
                "대표자",
                "담당자",
                "010-1234-5678",
                "dashboard-api@test.com"
        )));

        LocalDate today = LocalDate.now();
        Employee onLeaveEmployee = employeeRepository.save(createEmployee(department.getIdOrThrow(), "dashboard-onleave@abms.co", today));
        onLeaveEmployee.takeLeave();
        employeeRepository.save(createEmployee(department.getIdOrThrow(), "dashboard-active@abms.co", today.minusMonths(1)));

        projectRepository.save(createProject("PRJ-DASH-API-001", party.getIdOrThrow(), department.getIdOrThrow(), ProjectStatus.IN_PROGRESS));
        projectRepository.save(createProject("PRJ-DASH-API-002", party.getIdOrThrow(), department.getIdOrThrow(), ProjectStatus.COMPLETED));
        flushAndClear();

        try {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/dashboards/summary")
                            .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("doc-user")))
                    .andDo(document("dashboard/summary", responseBody()))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }

        DashboardSummaryResponse response = restTestClient.get()
                .uri("/api/dashboards/summary")
                .exchange()
                .expectStatus().isOk()
                .expectBody(DashboardSummaryResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.totalEmployeesCount()).isEqualTo(2);
        assertThat(response.activeProjectsCount()).isEqualTo(1);
        assertThat(response.newEmployeesCount()).isEqualTo(1);
        assertThat(response.onLeaveEmployeesCount()).isEqualTo(1);
    }

    private Employee createEmployee(Long departmentId, String email, LocalDate joinDate) {
        return Employee.create(
                departmentId,
                "홍길동",
                email,
                joinDate,
                LocalDate.of(1990, 1, 1),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        );
    }

    private Project createProject(String code, Long partyId, Long leadDepartmentId, ProjectStatus status) {
        return Project.create(
                partyId,
                leadDepartmentId,
                code,
                code,
                "대시보드 API 프로젝트",
                status,
                100_000_000L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );
    }

}
