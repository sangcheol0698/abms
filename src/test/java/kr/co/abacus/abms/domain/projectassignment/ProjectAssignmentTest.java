package kr.co.abacus.abms.domain.projectassignment;

import static kr.co.abacus.abms.domain.project.ProjectFixture.*;
import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.math.BigDecimal;
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

    @Test
    @DisplayName("MM 계산: 소수점 반올림 확인")
    void calculateManMonth_rounding() {
        // given
        Project project = createProject();
        ReflectionTestUtils.setField(project, "id", 1L);
        ReflectionTestUtils.setField(project, "period", new Period(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 20)));

        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 10);
        ProjectAssignmentCreateRequest projectAssignmentCreateRequest = new ProjectAssignmentCreateRequest(
            1L, 1L, AssignmentRole.DEV, startDate, endDate
        );

        ProjectAssignment assignment = ProjectAssignment.assign(project, projectAssignmentCreateRequest);

        // when
        BigDecimal result = assignment.calculateManMonth(LocalDate.of(2026, 1, 1));

        // then
        // 10 / 31 = 0.32258...
        // 로직상 소수점 첫째 자리까지 반올림(HALF_UP) 하므로 -> 0.3
        assertThat(result).isEqualByComparingTo("0.3");
    }

    @Test
    @DisplayName("MM 계산: 투입 기간이 아닌 달은 0.0이 반환된다")
    void calculateManMonth_zeroAssign() {
        // given
        Project project = createProject();
        ReflectionTestUtils.setField(project, "id", 1L);
        ReflectionTestUtils.setField(project, "period", new Period(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 20)));

        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 10);
        ProjectAssignmentCreateRequest projectAssignmentCreateRequest = new ProjectAssignmentCreateRequest(
            1L, 1L, AssignmentRole.DEV, startDate, endDate
        );

        ProjectAssignment assignment = ProjectAssignment.assign(project, projectAssignmentCreateRequest);

        // when
        // 1월 투입했는데, 2월 기준으로 계산하니 투입 기간 없음
        BigDecimal result = assignment.calculateManMonth(LocalDate.of(2026, 2, 1));

        // then
        assertThat(result).isEqualByComparingTo("0.0");
    }

    @Test
    @DisplayName("MM 계산: 해당 월 전체를 근무했으면 1.0이 반환된다 (28일/28일)")
    void calculateManMonth_FullMonth() {
        // given
        Project project = createProject();
        ReflectionTestUtils.setField(project, "id", 1L);
        ReflectionTestUtils.setField(project, "period", new Period(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31)));

        // 투입 기간: 1월 1일 ~ 3월 31일 (2월을 완전히 포함)
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 3, 31);
        ProjectAssignmentCreateRequest projectAssignmentCreateRequest = new ProjectAssignmentCreateRequest(
            1L, 1L, AssignmentRole.DEV, startDate, endDate
        );

        ProjectAssignment assignment = ProjectAssignment.assign(project, projectAssignmentCreateRequest);

        // when
        // 2월(28일) 기준 계산
        BigDecimal result = assignment.calculateManMonth(LocalDate.of(2026, 2, 1));

        // then
        // 28 / 28 = 1.0
        assertThat(result).isEqualByComparingTo("1.0");
    }
}