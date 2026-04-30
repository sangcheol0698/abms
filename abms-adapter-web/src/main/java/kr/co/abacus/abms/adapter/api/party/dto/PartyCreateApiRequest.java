package kr.co.abacus.abms.adapter.api.party.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.party.PartyCreateRequest;

public record PartyCreateApiRequest(
        @NotBlank @Size(max = 50) String name,
        @Nullable @Size(max = 30) String ceoName,
        @Nullable @Size(max = 30) String salesRepName,
        @Nullable @Size(max = 20) String salesRepPhone,
        @Nullable @Email @Size(max = 100) String salesRepEmail) {

    public PartyCreateRequest toDomainRequest() {
        return new PartyCreateRequest(name, ceoName, salesRepName, salesRepPhone, salesRepEmail);
    }

}
