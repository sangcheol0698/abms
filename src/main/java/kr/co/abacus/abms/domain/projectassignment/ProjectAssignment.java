package kr.co.abacus.abms.domain.projectassignment;

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

}
