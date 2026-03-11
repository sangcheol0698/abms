package kr.co.abacus.abms.application.party.dto;

import org.jspecify.annotations.Nullable;

public record PartyListItem(
        Long partyId,
        String name,
        @Nullable String ceo,
        @Nullable String manager,
        @Nullable String contact,
        @Nullable String email) {

}
