package kr.co.abacus.abms.adapter.api.party.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.party.PartyUpdateRequest;

public record PartyUpdateApiRequest(
        @NotBlank @Size(max = 50) String name,
        @Nullable @Size(max = 30) String ceoName,
        @Nullable @Size(max = 30) String salesRepName,
        @Nullable @Size(max = 20) String salesRepPhone,
        @Nullable @Email @Size(max = 100) String salesRepEmail) {

    public PartyUpdateRequest toDomainRequest() {
        return new PartyUpdateRequest(name, ceoName, salesRepName, salesRepPhone, salesRepEmail);
    }

}
