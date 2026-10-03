package kr.co.abacus.abms.party;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;
import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.domain.BaseEntity;
import kr.co.abacus.abms.common.domain.BusinessException;

/**
 * 협력사(고객사/파트너).
 */
@Entity
@Table(name = "tb_party")
@SQLRestriction("deleted = false")
public class Party extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    private @Nullable String ceoName;

    private @Nullable String salesRepName;

    private @Nullable String salesRepPhone;

    private @Nullable String salesRepEmail;

    protected Party() {
    }

    public static Party create(PartyInfo info) {
        Party party = new Party();
        party.apply(info);
        return party;
    }

    public void update(PartyInfo info) {
        apply(info);
    }

    private void apply(PartyInfo info) {
        String trimmed = info.name() == null ? "" : info.name().trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException("협력사명은 필수입니다.");
        }
        this.name = trimmed;
        this.ceoName = blankToNull(info.ceoName());
        this.salesRepName = blankToNull(info.salesRepName());
        this.salesRepPhone = blankToNull(info.salesRepPhone());
        this.salesRepEmail = blankToNull(info.salesRepEmail());
    }

    private static @Nullable String blankToNull(@Nullable String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public String getName() {
        return name;
    }

    public @Nullable String getCeoName() {
        return ceoName;
    }

    public @Nullable String getSalesRepName() {
        return salesRepName;
    }

    public @Nullable String getSalesRepPhone() {
        return salesRepPhone;
    }

    public @Nullable String getSalesRepEmail() {
        return salesRepEmail;
    }

    public record PartyInfo(
            String name,
            @Nullable String ceoName,
            @Nullable String salesRepName,
            @Nullable String salesRepPhone,
            @Nullable String salesRepEmail
    ) {
    }

}
