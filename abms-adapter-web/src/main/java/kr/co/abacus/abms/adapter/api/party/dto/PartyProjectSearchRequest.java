package kr.co.abacus.abms.adapter.api.party.dto;

import java.util.List;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.project.dto.ProjectSearchCondition;
import kr.co.abacus.abms.domain.project.ProjectStatus;

public record PartyProjectSearchRequest(
        @Nullable String name,
        @Nullable List<ProjectStatus> statuses
) {

    public ProjectSearchCondition toCondition(Long partyId) {
        return new ProjectSearchCondition(name, statuses, List.of(partyId), null, null);
    }
}
