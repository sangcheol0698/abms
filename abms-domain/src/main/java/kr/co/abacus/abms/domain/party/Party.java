package kr.co.abacus.abms.domain.party;

import static java.util.Objects.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.AbstractEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_party")
public class Party extends AbstractEntity {

    @Column(name = "party_name", nullable = false, length = 50)
    private String name;

    @Nullable
    @Column(name = "ceo_name", length = 30)
    private String ceoName;

    @Nullable
    @Column(name = "sales_rep_name", length = 30)
    private String salesRepName;

    @Nullable
    @Column(name = "sales_rep_phone", length = 20)
    private String salesRepPhone;

    @Nullable
    @Column(name = "sales_rep_email", length = 100)
    private String salesRepEmail;

    public static Party create(PartyCreateRequest request) {
        Party party = new Party();

        party.name = normalizeRequired(request.name());
        party.ceoName = normalizeOptional(request.ceoName());
        party.salesRepName = normalizeOptional(request.salesRepName());
        party.salesRepPhone = normalizeOptional(request.salesRepPhone());
        party.salesRepEmail = normalizeOptional(request.salesRepEmail());

        return party;
    }

    public void update(PartyUpdateRequest request) {
        this.name = normalizeRequired(request.name());
        this.ceoName = normalizeOptional(request.ceoName());
        this.salesRepName = normalizeOptional(request.salesRepName());
        this.salesRepPhone = normalizeOptional(request.salesRepPhone());
        this.salesRepEmail = normalizeOptional(request.salesRepEmail());
    }

    private static String normalizeRequired(String value) {
        String normalized = requireNonNull(value).trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("협력사명은 필수입니다.");
        }
        return normalized;
    }

    private static @Nullable String normalizeOptional(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

}
