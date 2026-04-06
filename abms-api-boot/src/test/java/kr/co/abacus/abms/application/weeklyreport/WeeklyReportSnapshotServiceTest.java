package kr.co.abacus.abms.application.weeklyreport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.projectassignment.outbound.ProjectAssignmentRepository;
import kr.co.abacus.abms.application.summary.inbound.MonthlyRevenueSummaryFinder;
import kr.co.abacus.abms.application.weeklyreport.dto.query.WeeklyReportSnapshot;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.domain.projectassignment.AssignmentRole;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentCreateRequest;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummary;
import kr.co.abacus.abms.domain.summary.MonthlyRevenueSummaryCreateRequest;

@DisplayName("주간 보고서 스냅샷 서비스")
class WeeklyReportSnapshotServiceTest {

    @Test
    @DisplayName("직원, 프로젝트, 리스크, 매출 스냅샷을 집계한다")
    void createSnapshot() {
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
        ProjectRepository projectRepository = mock(ProjectRepository.class);
        ProjectAssignmentRepository projectAssignmentRepository = mock(ProjectAssignmentRepository.class);
        MonthlyRevenueSummaryFinder monthlyRevenueSummaryFinder = mock(MonthlyRevenueSummaryFinder.class);

        Department platformTeam = Department.create("TEAM-PLATFORM", "플랫폼팀", DepartmentType.TEAM, null, null);
        Department serviceTeam = Department.create("TEAM-SERVICE", "서비스팀", DepartmentType.TEAM, null, null);
        ReflectionTestUtils.setField(platformTeam, "id", 1L);
        ReflectionTestUtils.setField(serviceTeam, "id", 2L);

        Employee joinedThisWeek = Employee.create(
                1L,
                "신규사원",
                "joined@abacus.co.kr",
                LocalDate.of(2026, 3, 24),
                LocalDate.of(1995, 1, 1),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        );
        Employee onLeave = Employee.create(
                1L,
                "휴직사원",
                "leave@abacus.co.kr",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(1992, 1, 1),
                EmployeePosition.SENIOR_ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.SKY_GLOW,
                null
        );
        onLeave.takeLeave();
        Employee resigned = Employee.create(
                2L,
                "퇴사사원",
                "resigned@abacus.co.kr",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(1991, 1, 1),
                EmployeePosition.SENIOR_ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.MID_LEVEL,
                EmployeeAvatar.SKY_GLOW,
                null
        );
        resigned.resign(LocalDate.of(2026, 3, 28));

        Project startedProject = Project.create(
                1L,
                1L,
                "PRJ-001",
                "신규 시작 프로젝트",
                null,
                ProjectStatus.IN_PROGRESS,
                100_000L,
                LocalDate.of(2026, 3, 24),
                LocalDate.of(2026, 4, 20)
        );
        ReflectionTestUtils.setField(startedProject, "id", 1L);
        Project endedProject = Project.create(
                1L,
                1L,
                "PRJ-002",
                "이번 주 종료 프로젝트",
                null,
                ProjectStatus.IN_PROGRESS,
                70_000L,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 28)
        );
        ReflectionTestUtils.setField(endedProject, "id", 2L);
        Project endingSoonProject = Project.create(
                1L,
                2L,
                "PRJ-003",
                "종료 임박 프로젝트",
                null,
                ProjectStatus.IN_PROGRESS,
                80_000L,
                LocalDate.of(2026, 3, 10),
                LocalDate.of(2026, 4, 2)
        );
        ReflectionTestUtils.setField(endingSoonProject, "id", 3L);
        Project completedProject = Project.create(
                1L,
                2L,
                "PRJ-004",
                "완료 프로젝트",
                null,
                ProjectStatus.COMPLETED,
                50_000L,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 3, 15)
        );
        ReflectionTestUtils.setField(completedProject, "id", 4L);

        ProjectAssignment endedAssignment = ProjectAssignment.assign(
                endedProject,
                new ProjectAssignmentCreateRequest(
                        2L,
                        2L,
                        AssignmentRole.DEV,
                        LocalDate.of(2026, 3, 1),
                        LocalDate.of(2026, 3, 28)
                )
        );

        MonthlyRevenueSummary monthlyRevenueSummary = MonthlyRevenueSummary.create(
                new MonthlyRevenueSummaryCreateRequest(
                        1L,
                        "PRJ-MONTH",
                        "월간집계",
                        1L,
                        "TEAM-PLATFORM",
                        "플랫폼팀",
                        LocalDate.of(2026, 3, 31),
                        kr.co.abacus.abms.domain.shared.Money.wons(1_000_000L),
                        kr.co.abacus.abms.domain.shared.Money.wons(600_000L),
                        kr.co.abacus.abms.domain.shared.Money.wons(400_000L)
                )
        );

        given(employeeRepository.search(org.mockito.ArgumentMatchers.any())).willReturn(List.of(joinedThisWeek, onLeave, resigned));
        given(departmentRepository.findAllByDeletedFalse()).willReturn(List.of(platformTeam, serviceTeam));
        given(projectRepository.findAllByDeletedFalse()).willReturn(List.of(startedProject, endedProject, endingSoonProject, completedProject));
        given(projectAssignmentRepository.findActiveAssignments(LocalDate.of(2026, 3, 23), LocalDate.of(2026, 3, 29)))
                .willReturn(List.of(endedAssignment));
        given(projectAssignmentRepository.findOverlappingAssignments(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.eq(LocalDate.of(2026, 3, 29)), org.mockito.ArgumentMatchers.eq(LocalDate.of(2026, 3, 29))))
                .willReturn(List.of());
        given(monthlyRevenueSummaryFinder.findOptionalByTargetMonth("202603")).willReturn(Optional.of(monthlyRevenueSummary));

        WeeklyReportSnapshotService snapshotService = new WeeklyReportSnapshotService(
                employeeRepository,
                departmentRepository,
                projectRepository,
                projectAssignmentRepository,
                monthlyRevenueSummaryFinder
        );

        WeeklyReportSnapshot snapshot = snapshotService.createSnapshot(
                LocalDate.of(2026, 3, 23),
                LocalDate.of(2026, 3, 29)
        );

        assertThat(snapshot.employees().totalEmployees()).isEqualTo(3);
        assertThat(snapshot.employees().joinedThisWeek()).isEqualTo(1);
        assertThat(snapshot.employees().resignedThisWeek()).isEqualTo(1);
        assertThat(snapshot.projects().startedThisWeek()).isEqualTo(1);
        assertThat(snapshot.projects().endedThisWeek()).isEqualTo(1);
        assertThat(snapshot.projects().assignmentChangeProjectCount()).isEqualTo(1);
        assertThat(snapshot.revenue().monthlySummaryAvailable()).isTrue();
        assertThat(snapshot.risks().endingSoonProjectNames()).contains("종료 임박 프로젝트");
    }

    @Test
    @DisplayName("월 매출 집계가 없어도 스냅샷 생성에 실패하지 않는다")
    void createSnapshotWithoutMonthlyRevenueSummary() {
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
        ProjectRepository projectRepository = mock(ProjectRepository.class);
        ProjectAssignmentRepository projectAssignmentRepository = mock(ProjectAssignmentRepository.class);
        MonthlyRevenueSummaryFinder monthlyRevenueSummaryFinder = mock(MonthlyRevenueSummaryFinder.class);

        given(employeeRepository.search(org.mockito.ArgumentMatchers.any())).willReturn(List.of());
        given(departmentRepository.findAllByDeletedFalse()).willReturn(List.of());
        given(projectRepository.findAllByDeletedFalse()).willReturn(List.of());
        given(projectAssignmentRepository.findActiveAssignments(LocalDate.of(2026, 3, 30), LocalDate.of(2026, 4, 5)))
                .willReturn(List.of());
        given(monthlyRevenueSummaryFinder.findOptionalByTargetMonth("202604")).willReturn(Optional.empty());

        WeeklyReportSnapshotService snapshotService = new WeeklyReportSnapshotService(
                employeeRepository,
                departmentRepository,
                projectRepository,
                projectAssignmentRepository,
                monthlyRevenueSummaryFinder
        );

        WeeklyReportSnapshot snapshot = snapshotService.createSnapshot(
                LocalDate.of(2026, 3, 30),
                LocalDate.of(2026, 4, 5)
        );

        assertThat(snapshot.revenue().monthlySummaryAvailable()).isFalse();
        assertThat(snapshot.revenue().targetMonth()).isEqualTo("202604");
        assertThat(snapshot.revenue().revenueAmount()).isNull();
    }
}
