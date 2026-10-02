package kr.co.abacus.abms.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.common.domain.Period;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeAvatar;
import kr.co.abacus.abms.employee.EmployeeGrade;
import kr.co.abacus.abms.employee.EmployeePosition;
import kr.co.abacus.abms.employee.EmployeeProfile;
import kr.co.abacus.abms.employee.EmployeeType;

class ProjectAssignmentTest {

    private final Project project = withId(Project.create("P-1", new Project.ProjectInfo(1L, 1L, "프로젝트", null,
            ProjectStatus.IN_PROGRESS, Money.wons(100_000_000), new Period(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 30)))), 10L);

    private final Employee employee = withId(Employee.create(new EmployeeProfile(1L, "개발자", "dev@test.co",
            LocalDate.of(2020, 1, 1), LocalDate.of(1990, 1, 1), EmployeePosition.ASSOCIATE, EmployeeType.FULL_TIME,
            EmployeeGrade.JUNIOR, EmployeeAvatar.SKY_GLOW, null)), 20L);

    @Test
    void 월_총일수_대비_투입일수로_MM을_계산한다() {
        ProjectAssignment assignment = ProjectAssignment.assign(project, employee, AssignmentRole.DEV,
                new Period(LocalDate.of(2026, 2, 15), LocalDate.of(2026, 4, 10)));

        assertThat(assignment.manMonth(YearMonth.of(2026, 1))).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(assignment.manMonth(YearMonth.of(2026, 2))).isEqualByComparingTo("0.5"); // 14 / 28
        assertThat(assignment.manMonth(YearMonth.of(2026, 3))).isEqualByComparingTo("1.0");
        assertThat(assignment.manMonth(YearMonth.of(2026, 4))).isEqualByComparingTo("0.3"); // 10 / 30
    }

    @Test
    void 투입_기간은_프로젝트_기간_안이어야_한다() {
        assertThatThrownBy(() -> ProjectAssignment.assign(project, employee, null,
                new Period(LocalDate.of(2025, 12, 31), LocalDate.of(2026, 3, 1))))
                .isInstanceOf(BusinessException.class).hasMessageContaining("프로젝트 시작일");
        assertThatThrownBy(() -> ProjectAssignment.assign(project, employee, null, new Period(LocalDate.of(2026, 1, 1), null)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("프로젝트 종료일");
    }

    @Test
    void 퇴사자는_퇴사일을_넘겨_투입할_수_없다() {
        employee.resign(LocalDate.of(2026, 3, 31));
        assertThatThrownBy(() -> ProjectAssignment.assign(project, employee, null,
                new Period(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 30))))
                .isInstanceOf(BusinessException.class).hasMessageContaining("퇴사일");
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

}
