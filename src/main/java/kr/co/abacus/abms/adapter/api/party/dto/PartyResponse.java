package kr.co.abacus.abms.adapter.api.party.dto;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.party.Party;

public record PartyResponse(
        Long partyId,
        String name,
        @Nullable String ceo,
        @Nullable String manager,
        @Nullable String contact,
        @Nullable String email) {

    public static PartyResponse from(Party party) {
        return new PartyResponse(
                party.getIdOrThrow(),
                party.getName(),
                party.getCeoName(),
                party.getSalesRepName(),
                party.getSalesRepPhone(),
                party.getSalesRepEmail());
    }

}
