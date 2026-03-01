package kr.co.abacus.abms.adapter.api.party.dto;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.party.PartyCreateRequest;

public record PartyCreateApiRequest(
        String name,
        @Nullable String ceoName,
        @Nullable String salesRepName,
        @Nullable String salesRepPhone,
        @Nullable String salesRepEmail) {

    public PartyCreateRequest toDomainRequest() {
        return new PartyCreateRequest(name, ceoName, salesRepName, salesRepPhone, salesRepEmail);
    }

}
