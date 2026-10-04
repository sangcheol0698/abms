package kr.co.abacus.abms.project;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;
import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.audit.Auditable;
import kr.co.abacus.abms.common.audit.Auditable.AuditRef;
import kr.co.abacus.abms.common.domain.BaseEntity;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Period;
import kr.co.abacus.abms.employee.Employee;

/**
 * 직원의 프로젝트 투입.
 */
@Entity
@Table(name = "tb_project_assignment")
@SQLRestriction("deleted = false")
public class ProjectAssignment extends BaseEntity implements Auditable {

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_role", length = 20)
    private @Nullable AssignmentRole role;

    @Embedded
    private Period period;

    protected ProjectAssignment() {
    }

    public static ProjectAssignment assign(Project project, Employee employee, @Nullable AssignmentRole role, Period period) {
        validate(project, employee, period);
        ProjectAssignment assignment = new ProjectAssignment();
        assignment.projectId = project.id();
        assignment.employeeId = employee.id();
        assignment.role = role;
        assignment.period = period;
        return assignment;
    }

    public void update(Project project, Employee employee, @Nullable AssignmentRole role, Period period) {
        validate(project, employee, period);
        this.employeeId = employee.id();
        this.role = role;
        this.period = period;
    }

    private static void validate(Project project, Employee employee, Period period) {
        Period projectPeriod = project.getPeriod();
        if (period.startDate().isBefore(projectPeriod.startDate())) {
            throw new BusinessException("투입 시작일은 프로젝트 시작일보다 빠를 수 없습니다.");
        }
        LocalDate projectEnd = projectPeriod.endDate();
        if (projectEnd != null && (period.endDate() == null || period.endDate().isAfter(projectEnd))) {
            throw new BusinessException("투입 종료일은 프로젝트 종료일보다 늦을 수 없습니다.");
        }
        LocalDate resignationDate = employee.getResignationDate();
        if (resignationDate != null && (period.endDate() == null || period.endDate().isAfter(resignationDate))) {
            throw new BusinessException("퇴사자의 투입 종료일은 퇴사일(" + resignationDate + ")을 넘길 수 없습니다.");
        }
        if (period.startDate().isBefore(employee.getJoinDate())) {
            throw new BusinessException("투입 시작일은 입사일(" + employee.getJoinDate() + ")보다 빠를 수 없습니다.");
        }
    }

    /** 해당 월의 투입 M/M. 월 총일수 대비 실제 투입일수 (소수 첫째 자리 반올림). */
    public BigDecimal manMonth(YearMonth month) {
        return period.manMonth(month);
    }

    public boolean isActiveOn(LocalDate date) {
        return period.contains(date);
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public @Nullable AssignmentRole getRole() {
        return role;
    }

    public Period getPeriod() {
        return period;
    }

    public static Period periodOf(LocalDate start, @Nullable LocalDate end) {
        return new Period(Objects.requireNonNull(start, "투입 시작일은 필수입니다."), end);
    }

    @Override
    public String auditLabel() {
        return "투입 인력";
    }

    @Override
    public String auditName() {
        return role == null ? "투입" : role.label();
    }

    @Override
    public AuditRef auditParent() {
        return new AuditRef("Project", projectId);
    }

}
