package kr.co.abacus.abms.adapter.api.party.dto;

import kr.co.abacus.abms.domain.party.Party;

public record PartyResponse(
        Long partyId,
        String name,
        String ceo,
        String manager,
        String contact,
        String email) {

    public static PartyResponse from(Party party) {
        return new PartyResponse(
                party.getId(),
                party.getName(),
                party.getCeoName(),
                party.getSalesRepName(),
                party.getSalesRepPhone(),
                party.getSalesRepEmail());
    }

}
