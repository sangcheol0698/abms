package kr.co.abacus.abms.adapter.api.projectAssignment.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import kr.co.abacus.abms.domain.projectassignment.ProjectAssignmentEndRequest;

public record ProjectAssignmentEndApiRequest(
        @NotNull LocalDate endDate
) {

    public ProjectAssignmentEndRequest toRequest() {
        return new ProjectAssignmentEndRequest(endDate);
    }
}
