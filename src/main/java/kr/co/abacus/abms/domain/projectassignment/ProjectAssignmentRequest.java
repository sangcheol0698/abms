package kr.co.abacus.abms.domain.projectassignment;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

public record ProjectAssignmentRequest(
        Long contractId,
        Long employeeId,
        @Nullable String assignmentRole,
        @Nullable Double assignmentRate,
        LocalDate startDate,
        @Nullable LocalDate endDate) {

}
