package kr.co.abacus.abms.application.projectassignment.inbound;

import kr.co.abacus.abms.application.employee.outbound.EmployeeRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.projectassignment.outbound.ProjectAssignmentRepository;
import kr.co.abacus.abms.domain.employee.Employee;
import kr.co.abacus.abms.domain.employee.EmployeeAvatar;
import kr.co.abacus.abms.domain.employee.EmployeeGrade;
import kr.co.abacus.abms.domain.employee.EmployeePosition;
import kr.co.abacus.abms.domain.employee.EmployeeType;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.projectassignment.AssignmentRole;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentCreateRequest;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentEndRequest;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentUpdateRequest;
import kr.co.abacus.abms.support.IntegrationTestBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static kr.co.abacus.abms.domain.project.ProjectFixture.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("프로젝트 투입 관리 (ProjectAssignmentManager)")
class ProjectAssignmentManagerTest extends IntegrationTestBase  {

    @Autowired
    private ProjectAssignmentManager projectAssignmentManager;

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    @DisplayName("프로젝트 투입 정보를 생성하고 저장한다")
    void create() {
        // given
        Project project = createProject();
        projectRepository.save(project);
        Employee employee = createEmployee("assignment-create@abms.co.kr");
        flushAndClear();

        ProjectAssignmentCreateRequest request = new ProjectAssignmentCreateRequest(
            project.getId(),
            employee.getIdOrThrow(),
            AssignmentRole.DEV,
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31)
        );

        // when
        ProjectAssignment result = projectAssignmentManager.create(request);

        // then
        List<ProjectAssignment> foundProjectAssignment = projectAssignmentRepository.findByProjectId(project.getId());
        assertThat(foundProjectAssignment).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트에 투입을 시도하면 예외가 발생한다")
    void create_projectNotFound() {
        // given
        Long nonExistentProjectId = 9999L;
        Employee employee = createEmployee("assignment-project-not-found@abms.co.kr");
        flushAndClear();

        ProjectAssignmentCreateRequest request = new ProjectAssignmentCreateRequest(
            nonExistentProjectId,
            employee.getIdOrThrow(),
            AssignmentRole.DEV,
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31)
        );

        // when & then
        assertThatThrownBy(() -> projectAssignmentManager.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("존재하지 않는 프로젝트입니다.");
    }

    @Test
    @DisplayName("프로젝트 투입 정보를 수정한다")
    void update() {
        Project project = createProject();
        projectRepository.save(project);
        Employee beforeEmployee = createEmployee("assignment-update-before@abms.co.kr");
        Employee afterEmployee = createEmployee("assignment-update-after@abms.co.kr");
        ProjectAssignment assignment = projectAssignmentRepository.save(ProjectAssignment.assign(project, new ProjectAssignmentCreateRequest(
                project.getId(),
                beforeEmployee.getIdOrThrow(),
                AssignmentRole.DEV,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 30)
        )));
        flushAndClear();

        ProjectAssignment result = projectAssignmentManager.update(assignment.getId(), new ProjectAssignmentUpdateRequest(
                afterEmployee.getIdOrThrow(),
                AssignmentRole.PM,
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 5, 31)
        ));

        assertThat(result.getEmployeeId()).isEqualTo(afterEmployee.getIdOrThrow());
        assertThat(result.getRole()).isEqualTo(AssignmentRole.PM);
        assertThat(result.getPeriod().startDate()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(result.getPeriod().endDate()).isEqualTo(LocalDate.of(2024, 5, 31));
    }

    @Test
    @DisplayName("프로젝트 투입 종료일을 수정해 종료 처리한다")
    void end() {
        Project project = createProject();
        projectRepository.save(project);
        Employee employee = createEmployee("assignment-end@abms.co.kr");
        ProjectAssignment assignment = projectAssignmentRepository.save(ProjectAssignment.assign(project, new ProjectAssignmentCreateRequest(
                project.getId(),
                employee.getIdOrThrow(),
                AssignmentRole.DEV,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        )));
        flushAndClear();

        ProjectAssignment result = projectAssignmentManager.end(assignment.getId(), new ProjectAssignmentEndRequest(
                LocalDate.of(2024, 7, 31)
        ));

        assertThat(result.getPeriod().startDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(result.getPeriod().endDate()).isEqualTo(LocalDate.of(2024, 7, 31));
    }

    @Test
    @DisplayName("같은 프로젝트의 동일 직원 배정 기간이 겹치면 예외가 발생한다")
    void create_overlap() {
        Project project = createProject();
        projectRepository.save(project);
        Employee employee = createEmployee("assignment-overlap@abms.co.kr");
        projectAssignmentRepository.save(ProjectAssignment.assign(project, new ProjectAssignmentCreateRequest(
                project.getId(),
                employee.getIdOrThrow(),
                AssignmentRole.DEV,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 30)
        )));
        flushAndClear();

        assertThatThrownBy(() -> projectAssignmentManager.create(new ProjectAssignmentCreateRequest(
                project.getId(),
                employee.getIdOrThrow(),
                AssignmentRole.PM,
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 8, 31)
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("동일 프로젝트에 중복되는 투입 기간이 존재합니다.");
    }

    @Test
    @DisplayName("퇴사일 이후 종료되는 프로젝트 투입 생성은 예외가 발생한다")
    void create_afterResignationDate() {
        Project project = createProject();
        projectRepository.save(project);
        Employee employee = createEmployee("assignment-resigned-create@abms.co.kr");
        employee.resign(LocalDate.of(2024, 6, 30));
        flushAndClear();

        assertThatThrownBy(() -> projectAssignmentManager.create(new ProjectAssignmentCreateRequest(
                project.getId(),
                employee.getIdOrThrow(),
                AssignmentRole.DEV,
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 7, 31)
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("퇴사일 이후에는 프로젝트 투입을 수립할 수 없습니다.");
    }

    @Test
    @DisplayName("퇴사자의 종료일 없는 프로젝트 투입 생성은 예외가 발생한다")
    void create_openEndedAfterResignationDate() {
        Project project = createProject();
        projectRepository.save(project);
        Employee employee = createEmployee("assignment-resigned-open@abms.co.kr");
        employee.resign(LocalDate.of(2024, 6, 30));
        flushAndClear();

        assertThatThrownBy(() -> projectAssignmentManager.create(new ProjectAssignmentCreateRequest(
                project.getId(),
                employee.getIdOrThrow(),
                AssignmentRole.DEV,
                LocalDate.of(2024, 6, 1),
                null
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("퇴사일 이후에는 프로젝트 투입을 수립할 수 없습니다.");
    }

    @Test
    @DisplayName("퇴사일 이후 종료되도록 프로젝트 투입을 수정하면 예외가 발생한다")
    void update_afterResignationDate() {
        Project project = createProject();
        projectRepository.save(project);
        Employee employee = createEmployee("assignment-resigned-update@abms.co.kr");
        employee.resign(LocalDate.of(2024, 6, 30));
        ProjectAssignment assignment = projectAssignmentRepository.save(ProjectAssignment.assign(project, new ProjectAssignmentCreateRequest(
                project.getId(),
                employee.getIdOrThrow(),
                AssignmentRole.DEV,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 30)
        )));
        flushAndClear();

        assertThatThrownBy(() -> projectAssignmentManager.update(assignment.getId(), new ProjectAssignmentUpdateRequest(
                employee.getIdOrThrow(),
                AssignmentRole.DEV,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 7, 31)
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("퇴사일 이후에는 프로젝트 투입을 수립할 수 없습니다.");
    }

    @Test
    @DisplayName("퇴사일까지만 프로젝트 투입을 수립할 수 있다")
    void create_untilResignationDate() {
        Project project = createProject();
        projectRepository.save(project);
        Employee employee = createEmployee("assignment-resigned-allowed@abms.co.kr");
        employee.resign(LocalDate.of(2024, 6, 30));
        flushAndClear();

        ProjectAssignment result = projectAssignmentManager.create(new ProjectAssignmentCreateRequest(
                project.getId(),
                employee.getIdOrThrow(),
                AssignmentRole.DEV,
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 6, 30)
        ));

        assertThat(result.getPeriod().endDate()).isEqualTo(LocalDate.of(2024, 6, 30));
    }

    private Employee createEmployee(String email) {
        return employeeRepository.save(Employee.create(
                1L,
                "투입직원",
                email,
                LocalDate.of(2023, 1, 1),
                LocalDate.of(1990, 1, 1),
                EmployeePosition.ASSOCIATE,
                EmployeeType.FULL_TIME,
                EmployeeGrade.JUNIOR,
                EmployeeAvatar.SKY_GLOW,
                null
        ));
    }
}
