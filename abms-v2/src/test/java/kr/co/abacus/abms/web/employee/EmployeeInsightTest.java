package kr.co.abacus.abms.web.employee;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.common.domain.Period;
import kr.co.abacus.abms.employee.Employee;
import kr.co.abacus.abms.employee.EmployeeGrade;
import kr.co.abacus.abms.employee.EmployeeJob;
import kr.co.abacus.abms.employee.EmployeePosition;
import kr.co.abacus.abms.employee.EmployeeProfile;
import kr.co.abacus.abms.employee.EmployeeType;
import kr.co.abacus.abms.employee.WorkType;
import kr.co.abacus.abms.project.AssignmentRole;
import kr.co.abacus.abms.project.Project;
import kr.co.abacus.abms.project.ProjectAssignment;
import kr.co.abacus.abms.project.ProjectStatus;

class EmployeeInsightTest {

    private final Employee employee = withId(Employee.create(new EmployeeProfile(1L, "개발자", "dev@test.co",
            LocalDate.of(2022, 3, 1), LocalDate.of(1990, 1, 1), EmployeePosition.SENIOR_ASSOCIATE, EmployeeType.FULL_TIME,
            EmployeeGrade.MID_LEVEL, null, null, LocalDate.of(2018, 3, 1), EmployeeJob.DEVELOPMENT,
            "Java", WorkType.CLIENT_SITE)), 20L);

    private final Project project = withId(Project.create("P-1", new Project.ProjectInfo(1L, 1L, "프로젝트", null,
            ProjectStatus.IN_PROGRESS, Money.wons(100_000_000), new Period(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))), 10L);

    @Test
    void 월별_투입과_이번_달_가용_여부와_경력을_계산한다() {
        ProjectAssignment first = ProjectAssignment.assign(project, employee, AssignmentRole.DEV,
                new Period(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 15)));   // 1월 1.0, 2월 1.0, 3월 0.5
        ProjectAssignment next = ProjectAssignment.assign(project, employee, AssignmentRole.DEV,
                new Period(LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 30)));   // 예정 (기준일 4/10)

        EmployeeInsight insight = EmployeeInsight.of(employee, List.of(first, next), LocalDate.of(2026, 4, 10));

        assertThat(insight.monthlyMm().get(0)).isEqualByComparingTo("1.0");
        assertThat(insight.monthlyMm().get(2)).isEqualByComparingTo("0.5");
        assertThat(insight.yearMm()).isEqualByComparingTo("4.5");
        assertThat(insight.currentMonthMm()).isEqualByComparingTo("0");
        assertThat(insight.available()).isTrue();
        assertThat(insight.activeAssignments()).isZero();
        assertThat(insight.upcomingAssignments()).isEqualTo(1);
        assertThat(insight.careerMonths()).isEqualTo(97);  // 2018-03-01 ~ 2026-04-10
        assertThat(insight.tenureMonths()).isEqualTo(49);  // 2022-03-01 ~ 2026-04-10
        assertThat(EmployeeInsight.years(97)).isEqualTo("8년 1개월");
    }

    private static <T> T withId(T entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

}
