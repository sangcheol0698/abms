package kr.co.abacus.abms.application.projectassignment.inbound;

import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.projectassignment.outbound.ProjectAssignmentRepository;
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

    @Test
    @DisplayName("프로젝트 투입 정보를 생성하고 저장한다")
    void create() {
        // given
        Project project = createProject();
        projectRepository.save(project);
        flushAndClear();

        ProjectAssignmentCreateRequest request = new ProjectAssignmentCreateRequest(
            project.getId(),
            100L,
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
        ProjectAssignmentCreateRequest request = new ProjectAssignmentCreateRequest(
            nonExistentProjectId,
            100L,
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
        ProjectAssignment assignment = projectAssignmentRepository.save(ProjectAssignment.assign(project, new ProjectAssignmentCreateRequest(
                project.getId(),
                100L,
                AssignmentRole.DEV,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 30)
        )));
        flushAndClear();

        ProjectAssignment result = projectAssignmentManager.update(assignment.getId(), new ProjectAssignmentUpdateRequest(
                101L,
                AssignmentRole.PM,
                LocalDate.of(2024, 2, 1),
                LocalDate.of(2024, 5, 31)
        ));

        assertThat(result.getEmployeeId()).isEqualTo(101L);
        assertThat(result.getRole()).isEqualTo(AssignmentRole.PM);
        assertThat(result.getPeriod().startDate()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(result.getPeriod().endDate()).isEqualTo(LocalDate.of(2024, 5, 31));
    }

    @Test
    @DisplayName("프로젝트 투입 종료일을 수정해 종료 처리한다")
    void end() {
        Project project = createProject();
        projectRepository.save(project);
        ProjectAssignment assignment = projectAssignmentRepository.save(ProjectAssignment.assign(project, new ProjectAssignmentCreateRequest(
                project.getId(),
                100L,
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
        projectAssignmentRepository.save(ProjectAssignment.assign(project, new ProjectAssignmentCreateRequest(
                project.getId(),
                100L,
                AssignmentRole.DEV,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 30)
        )));
        flushAndClear();

        assertThatThrownBy(() -> projectAssignmentManager.create(new ProjectAssignmentCreateRequest(
                project.getId(),
                100L,
                AssignmentRole.PM,
                LocalDate.of(2024, 6, 1),
                LocalDate.of(2024, 8, 31)
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("동일 프로젝트에 중복되는 투입 기간이 존재합니다.");
    }
}
