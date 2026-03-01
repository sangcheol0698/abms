package kr.co.abacus.abms.application.projectassignment.inbound;


import kr.co.abacus.abms.application.projectassignment.outbound.ProjectAssignmentRepository;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.projectassignment.AssignmentRole;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentCreateRequest;
import kr.co.abacus.abms.domain.shared.Period;
import kr.co.abacus.abms.support.IntegrationTestBase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static kr.co.abacus.abms.domain.project.ProjectFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("프로젝트 할당 조회 (ProjectAssignmentFinder)")
class ProjectAssignmentFinderTest extends IntegrationTestBase {

    @Autowired
    private ProjectAssignmentFinder projectAssignmentFinder;

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Test
    @DisplayName("프로젝트 ID로 투입 정보를 조회한다")
    void findByProjectId() {
        // given
        Long projectId = 1L;
        Long employeeId1 = 100L;
        Long employeeId2 = 101L;

        Project project = createProject();
        ReflectionTestUtils.setField(project, "id", projectId);
        ReflectionTestUtils.setField(project, "period", new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)));

        ProjectAssignmentCreateRequest request1 = new ProjectAssignmentCreateRequest(
            projectId,
            employeeId1,
            AssignmentRole.DEV,
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 6, 30)
        );
        ProjectAssignment assignment1 = ProjectAssignment.assign(project, request1);

        ProjectAssignmentCreateRequest request2 = new ProjectAssignmentCreateRequest(
            projectId,
            employeeId2,
            AssignmentRole.DEV,
            LocalDate.of(2024, 7, 1),
            LocalDate.of(2024, 12, 31)
        );
        ProjectAssignment assignment2 = ProjectAssignment.assign(project, request2);

        projectAssignmentRepository.saveAll(List.of(assignment1, assignment2));
        flushAndClear();

        // when
        List<ProjectAssignment> foundProjectAssignment = projectAssignmentFinder.findByProjectId(projectId);

        // then
        assertThat(foundProjectAssignment).hasSize(2);
        assertThat(foundProjectAssignment)
            .extracting("employeeId")
            .containsExactlyInAnyOrder(employeeId1, employeeId2);
    }
}