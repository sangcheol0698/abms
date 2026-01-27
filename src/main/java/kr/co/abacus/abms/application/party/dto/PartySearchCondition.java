package kr.co.abacus.abms.application.party.dto;

import org.jspecify.annotations.Nullable;

public record PartySearchCondition(
        @Nullable String name
) {
}
