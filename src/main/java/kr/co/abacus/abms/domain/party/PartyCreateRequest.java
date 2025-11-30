package kr.co.abacus.abms.domain.party;

import org.jspecify.annotations.Nullable;

public record PartyCreateRequest(
    String name,
    @Nullable String ceoName,
    @Nullable String salesRepName,
    @Nullable String salesRepPhone,
    @Nullable String salesRepEmail
) {
}
