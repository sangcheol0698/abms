package kr.co.abacus.abms.domain.projectassignment;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

public record ProjectAssignmentUpdateRequest(
        Long employeeId,
        @Nullable AssignmentRole role,
        LocalDate startDate,
        @Nullable LocalDate endDate
) {

}
