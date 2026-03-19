package kr.co.abacus.abms.adapter.api.projectAssignment.dto;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import jakarta.validation.constraints.NotNull;
import kr.co.abacus.abms.domain.projectassignment.AssignmentRole;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentUpdateRequest;

public record ProjectAssignmentUpdateApiRequest(
        @NotNull Long employeeId,
        @Nullable AssignmentRole role,
        @NotNull LocalDate startDate,
        @Nullable LocalDate endDate
) {

    public ProjectAssignmentUpdateRequest toRequest() {
        return new ProjectAssignmentUpdateRequest(employeeId, role, startDate, endDate);
    }
}
