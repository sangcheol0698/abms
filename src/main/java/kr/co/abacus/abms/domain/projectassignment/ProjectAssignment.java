package kr.co.abacus.abms.domain.projectassignment;

import static java.util.Objects.*;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.AbstractEntity;
import kr.co.abacus.abms.domain.shared.Period;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "project_assignment")
public class ProjectAssignment extends AbstractEntity {

    @Column(name = "contract_id", nullable = false)
    private UUID contractId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Nullable
    @Column(name = "assignment_role", length = 50)
    private String assignmentRole;

    @Nullable
    @Column(name = "assignment_rate")
    private Double assignmentRate;

    @Embedded
    @AttributeOverride(name = "startDate", column = @Column(name = "start_date", nullable = false))
    @AttributeOverride(name = "endDate", column = @Column(name = "end_date"))
    private Period period;

    public static ProjectAssignment assign(ProjectAssignmentRequest request) {
        ProjectAssignment assignment = new ProjectAssignment();

        assignment.contractId = requireNonNull(request.contractId());
        assignment.employeeId = requireNonNull(request.employeeId());
        assignment.assignmentRole = request.assignmentRole();
        assignment.assignmentRate = request.assignmentRate();
        assignment.period = new Period(requireNonNull(request.startDate()), request.endDate());

        // 투입률 검증
        if (assignment.assignmentRate != null) {
            if (assignment.assignmentRate < 0.0 || assignment.assignmentRate > 1.0) {
                throw new IllegalArgumentException("투입률은 0.0 이상 1.0 이하여야 합니다");
            }
        }

        return assignment;
    }

    public void unassign(LocalDate endDate) {
        requireNonNull(endDate, "종료일은 필수입니다");
        this.period = new Period(this.period.startDate(), endDate);
    }

    public void updateRate(Double newRate) {
        if (newRate != null) {
            if (newRate < 0.0 || newRate > 1.0) {
                throw new IllegalArgumentException("투입률은 0.0 이상 1.0 이하여야 합니다");
            }
        }
        this.assignmentRate = newRate;
    }

}
