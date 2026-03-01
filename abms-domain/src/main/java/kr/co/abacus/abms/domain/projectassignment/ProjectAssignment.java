package kr.co.abacus.abms.domain.projectassignment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.project.Project;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.AbstractEntity;
import kr.co.abacus.abms.domain.shared.Period;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_project_assignment")
public class ProjectAssignment extends AbstractEntity {

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_role", length = 20)
    private AssignmentRole role;

    @Embedded
    @AttributeOverride(name = "startDate", column = @Column(name = "start_date", nullable = false))
    @AttributeOverride(name = "endDate", column = @Column(name = "end_date"))
    private Period period;

    public static ProjectAssignment assign(Project project, ProjectAssignmentCreateRequest request) {
        ProjectAssignment assignment = new ProjectAssignment();

        // 프로젝트 할당 기간 유효성 검사
        if (request.startDate().isBefore(project.getPeriod().startDate())) {
            throw new IllegalArgumentException("투입 시작일은 프로젝트 시작일보다 빠를 수 없습니다.");
        }
        if (project.getPeriod().endDate() != null) {
            if (request.endDate() == null || request.endDate().isAfter(project.getPeriod().endDate())) {
                throw new IllegalArgumentException("투입 종료일은 프로젝트 종료일보다 늦을 수 없습니다.");
            }
        }

        assignment.projectId = Objects.requireNonNull(request.projectId());
        assignment.employeeId = Objects.requireNonNull(request.employeeId());
        assignment.role = request.role() != null ? request.role() : null;
        assignment.period = new Period(Objects.requireNonNull(request.startDate()), request.endDate());

        return assignment;
    }

    /**
     * 특정 월(targetDate)에 대한 투입 M/M(Man-Month)를 계산합니다.
     * 예: 2월(28일) 중 14일 투입 시 -> 0.5 return
     */
    public BigDecimal calculateManMonth(LocalDate targetDate) {
        // 1. 기준 월의 시작일과 종료일 구하기
        LocalDate monthStart = targetDate.withDayOfMonth(1);
        LocalDate monthEnd = targetDate.withDayOfMonth(targetDate.lengthOfMonth());

        // 2. targetDate 에 해당하는 투입 기간 구하기

        // 실제 시작일 = max(투입 시작일, 월 시작일)
        LocalDate realStart = this.period.startDate().isAfter(monthStart) ?
            this.period.startDate() : monthStart;

        // 실제 종료일 = min(투입 종료일, 월 종료일)
        // 투입 종료일이 null(진행 중)이면 월 종료일을 사용
        LocalDate effectiveEndDate = (this.period.endDate() != null) ?
            this.period.endDate() : LocalDate.MAX;

        LocalDate realEnd = effectiveEndDate.isBefore(monthEnd) ?
            effectiveEndDate : monthEnd;

        // 3. targetDate 에 해당하는 투입이 없는 경우
        if (realStart.isAfter(realEnd)) {
            return BigDecimal.ZERO;
        }

        // 4. 근무 일수 계산 (시작일 포함이므로 +1)
        long workedDays = ChronoUnit.DAYS.between(realStart, realEnd) + 1;
        long totalDaysInMonth = monthEnd.lengthOfMonth();

        // 5. M/M 계산: 근무일수 / 월 총일수 (소수점 4자리에서 반올림하여 1자리까지 표시)
        // 예: 14 / 28 = 0.5
        return BigDecimal.valueOf(workedDays)
            .divide(BigDecimal.valueOf(totalDaysInMonth), 1, RoundingMode.HALF_UP);
    }

}
