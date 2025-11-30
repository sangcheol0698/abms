package kr.co.abacus.abms.domain.projectassignment;

import java.time.LocalDate;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

public record ProjectAssignmentRequest(
    UUID contractId,
    UUID employeeId,
    @Nullable String assignmentRole,
    @Nullable Double assignmentRate,
    LocalDate startDate,
    @Nullable LocalDate endDate
) {
}
