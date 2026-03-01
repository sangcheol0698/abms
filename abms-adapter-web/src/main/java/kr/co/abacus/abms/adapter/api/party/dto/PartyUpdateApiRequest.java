package kr.co.abacus.abms.adapter.api.party.dto;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.party.PartyUpdateRequest;

public record PartyUpdateApiRequest(
        String name,
        @Nullable String ceoName,
        @Nullable String salesRepName,
        @Nullable String salesRepPhone,
        @Nullable String salesRepEmail) {

    public PartyUpdateRequest toDomainRequest() {
        return new PartyUpdateRequest(name, ceoName, salesRepName, salesRepPhone, salesRepEmail);
    }

}
