package kr.co.abacus.abms.domain.projectassignment;

import static kr.co.abacus.abms.domain.project.ProjectFixture.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.time.LocalDate;
import java.util.Objects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.shared.Period;

@DisplayName("프로젝트 투입 (ProjectAssignment)")
class ProjectAssignmentTest {


    @Test
    @DisplayName("프로젝트 투입 정보를 생성한다")
    void assign() {
        // given
        Project project = createProject();
        ReflectionTestUtils.setField(project, "id", 1L);
        // 테스트를 위해 프로젝트 기간을 명시적으로 설정 (2024-01-01 ~ 2024-12-31)
        ReflectionTestUtils.setField(project, "period", new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)));

        ProjectAssignmentCreateRequest request = new ProjectAssignmentCreateRequest(
            1L, 1L, AssignmentRole.DEV, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)
        );

        // when
        ProjectAssignment assignment = ProjectAssignment.assign(project, request);

        // then
        assertThat(assignment.getProjectId()).isEqualTo(project.getId());
        assertThat(assignment.getEmployeeId()).isEqualTo(1L);
        assertThat(Objects.requireNonNull(assignment.getRole())).isEqualTo(AssignmentRole.DEV);
        assertThat(assignment.getPeriod()).isEqualTo(new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)));

        // 투입 기간이 프로젝트 기간 내에 포함되는지 검증
        assertThat(assignment.getPeriod().startDate()).isAfterOrEqualTo(project.getPeriod().startDate());
        assertThat(assignment.getPeriod().endDate()).isBeforeOrEqualTo(project.getPeriod().endDate());
    }

    @Test
    @DisplayName("투입 기간의 시작일이 종료일보다 늦으면 예외가 발생한다")
    void assign_invalidPeriod() {
        // given
        Project project = createProject();

        ProjectAssignmentCreateRequest request = new ProjectAssignmentCreateRequest(
            1L, 1L, AssignmentRole.DEV, LocalDate.of(2024, 12, 31), LocalDate.of(2024, 1, 1)
        );

        // when & then
        assertThatThrownBy(() -> ProjectAssignment.assign(project, request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("투입 시작일이 프로젝트 시작일보다 빠르면 예외가 발생한다")
    void assign_startDateBeforeProjectStart() {
        // given
        Project project = createProject();
        ReflectionTestUtils.setField(project, "period", new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)));

        ProjectAssignmentCreateRequest request = new ProjectAssignmentCreateRequest(
            1L, 1L, AssignmentRole.DEV, LocalDate.of(2023, 12, 31), LocalDate.of(2024, 12, 31)
        );

        // when & then
        assertThatThrownBy(() -> ProjectAssignment.assign(project, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("투입 시작일은 프로젝트 시작일보다 빠를 수 없습니다");
    }

    @Test
    @DisplayName("투입 종료일이 프로젝트 종료일보다 늦으면 예외가 발생한다")
    void assign_endDateAfterProjectEnd() {
        // given
        Project project = createProject();
        ReflectionTestUtils.setField(project, "period", new Period(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31)));

        ProjectAssignmentCreateRequest request = new ProjectAssignmentCreateRequest(
            1L, 1L, AssignmentRole.DEV, LocalDate.of(2024, 1, 1), LocalDate.of(2025, 1, 1)
        );

        // when & then
        assertThatThrownBy(() -> ProjectAssignment.assign(project, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("투입 종료일은 프로젝트 종료일보다 늦을 수 없습니다");
    }
}