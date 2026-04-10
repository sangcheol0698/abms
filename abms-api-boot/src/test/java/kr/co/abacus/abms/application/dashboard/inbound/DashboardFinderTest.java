package kr.co.abacus.abms.application.dashboard.inbound;

import static org.assertj.core.api.Assertions.*;

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
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("대시보드 조회 (DashboardFinder)")
class DashboardFinderTest extends IntegrationTestBase {

    @Autowired
    private DashboardFinder dashboardFinder;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    @DisplayName("대시보드 집계는 삭제되지 않은 직원과 프로젝트만 반영한다")
    void getDashboardSummary() {
        Department department = departmentRepository.save(Department.create("TEAM-DASHBOARD", "대시보드팀", DepartmentType.TEAM, null, null));
        Party party = partyRepository.save(Party.create(new PartyCreateRequest(
                "대시보드 협력사",
                "대표자",
                "담당자",
                "010-1234-5678",
                "dashboard@test.com"
        )));

        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate previousMonth = firstDayOfMonth.minusDays(1);

        employeeRepository.save(createEmployee(department.getIdOrThrow(), "active-current@abms.co", firstDayOfMonth.plusDays(Math.min(1, today.lengthOfMonth() - 1))));

        Employee onLeaveEmployee = employeeRepository.save(createEmployee(department.getIdOrThrow(), "leave-current@abms.co", today));
        onLeaveEmployee.takeLeave();

        employeeRepository.save(createEmployee(department.getIdOrThrow(), "resigned-previous@abms.co", previousMonth));

        Employee deletedEmployee = employeeRepository.save(createEmployee(department.getIdOrThrow(), "deleted-current@abms.co", today));
        deletedEmployee.softDelete(null);

        projectRepository.save(createProject("PRJ-DASH-001", party.getIdOrThrow(), department.getIdOrThrow(), ProjectStatus.IN_PROGRESS));
        projectRepository.save(createProject("PRJ-DASH-002", party.getIdOrThrow(), department.getIdOrThrow(), ProjectStatus.COMPLETED));

        Project deletedProject = projectRepository.save(createProject("PRJ-DASH-003", party.getIdOrThrow(), department.getIdOrThrow(), ProjectStatus.IN_PROGRESS));
        deletedProject.softDelete(null);
        flushAndClear();

        DashboardSummaryResponse summary = dashboardFinder.getDashboardSummary(today.getYear());

        assertThat(summary.totalEmployeesCount()).isEqualTo(3);
        assertThat(summary.activeProjectsCount()).isEqualTo(1);
        assertThat(summary.completedProjectsCount()).isEqualTo(0);
        int expectedNewEmployeesCount = previousMonth.getYear() == today.getYear() ? 3 : 2;
        assertThat(summary.newEmployeesCount()).isEqualTo(expectedNewEmployeesCount);
        assertThat(summary.yearRevenue()).isEqualTo(0L);
        assertThat(summary.yearProfit()).isEqualTo(0L);
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
                "대시보드 프로젝트",
                status,
                100_000_000L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );
    }

}
