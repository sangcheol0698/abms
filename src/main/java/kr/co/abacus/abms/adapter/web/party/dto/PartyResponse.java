package kr.co.abacus.abms.adapter.web.party.dto;

import java.util.UUID;

import kr.co.abacus.abms.domain.party.Party;

public record PartyResponse(
        UUID partyId,
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
