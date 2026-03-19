package kr.co.abacus.abms.application.projectassignment.inbound;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import kr.co.abacus.abms.application.department.outbound.DepartmentRepository;
import kr.co.abacus.abms.application.party.outbound.PartyRepository;
import kr.co.abacus.abms.application.project.outbound.ProjectRepository;
import kr.co.abacus.abms.application.projectassignment.outbound.ProjectAssignmentRepository;
import kr.co.abacus.abms.domain.department.Department;
import kr.co.abacus.abms.domain.department.DepartmentType;
import kr.co.abacus.abms.domain.party.Party;
import kr.co.abacus.abms.domain.party.PartyCreateRequest;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.domain.projectassignment.AssignmentRole;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignment;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentCreateRequest;
import kr.co.abacus.abms.support.IntegrationTestBase;

@DisplayName("직원 프로젝트 조회 (ProjectAssignmentFinder)")
class ProjectAssignmentFinderTest extends IntegrationTestBase {

    @Autowired
    private ProjectAssignmentFinder projectAssignmentFinder;

    @Autowired
    private ProjectAssignmentRepository projectAssignmentRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private PartyRepository partyRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    @DisplayName("직원의 프로젝트 배정 이력을 시작일 역순과 상태로 반환한다")
    void findByEmployeeId() {
        Party party = partyRepository.save(Party.create(new PartyCreateRequest("아바쿠스", null, null, null, null)));
        Department department = departmentRepository.save(Department.create("DEV", "개발팀", DepartmentType.TEAM, null, null));
        Long employeeId = 10L;
        LocalDate today = LocalDate.now();

        Project scheduledProject = projectRepository.save(Project.create(
                party.getId(), department.getIdOrThrow(), "PRJ-S", "예정 프로젝트", null,
                ProjectStatus.SCHEDULED, 1000000L, today.plusDays(1), today.plusMonths(1)
        ));
        Project currentProject = projectRepository.save(Project.create(
                party.getId(), department.getIdOrThrow(), "PRJ-C", "진행 프로젝트", null,
                ProjectStatus.IN_PROGRESS, 1000000L, today.minusMonths(1), today.plusMonths(1)
        ));
        Project endedProject = projectRepository.save(Project.create(
                party.getId(), department.getIdOrThrow(), "PRJ-E", "종료 프로젝트", null,
                ProjectStatus.COMPLETED, 1000000L, today.minusMonths(3), today.minusMonths(1)
        ));

        projectAssignmentRepository.save(ProjectAssignment.assign(currentProject, new ProjectAssignmentCreateRequest(
                currentProject.getIdOrThrow(), employeeId, AssignmentRole.DEV, today.minusDays(10), today.plusDays(10)
        )));
        projectAssignmentRepository.save(ProjectAssignment.assign(scheduledProject, new ProjectAssignmentCreateRequest(
                scheduledProject.getIdOrThrow(), employeeId, AssignmentRole.PM, today.plusDays(1), today.plusDays(20)
        )));
        projectAssignmentRepository.save(ProjectAssignment.assign(endedProject, new ProjectAssignmentCreateRequest(
                endedProject.getIdOrThrow(), employeeId, AssignmentRole.PL, today.minusMonths(2), today.minusMonths(1)
        )));
        flushAndClear();

        var result = projectAssignmentFinder.findByEmployeeId(
                new kr.co.abacus.abms.application.projectassignment.dto.EmployeeProjectSearchCondition(
                        employeeId,
                        null,
                        null,
                        null
                ),
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent().get(0).projectCode()).isEqualTo("PRJ-S");
        assertThat(result.getContent().get(0).assignmentStatus()).isEqualTo("SCHEDULED");
        assertThat(result.getContent().get(1).projectCode()).isEqualTo("PRJ-C");
        assertThat(result.getContent().get(1).assignmentStatus()).isEqualTo("CURRENT");
        assertThat(result.getContent().get(2).projectCode()).isEqualTo("PRJ-E");
        assertThat(result.getContent().get(2).assignmentStatus()).isEqualTo("ENDED");
    }
}
