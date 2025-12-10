package kr.co.abacus.abms.adapter.web.department.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record EmployeeAssignTeamLeaderRequest(
    @NotNull UUID leaderEmployeeId
) {

}
