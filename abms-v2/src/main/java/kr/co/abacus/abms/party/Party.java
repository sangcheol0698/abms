package kr.co.abacus.abms.party;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PartyType partyType = PartyType.CLIENT;

    /** 사업자등록번호 (000-00-00000) */
    @Column(length = 12)
    private @Nullable String businessNumber;

    private @Nullable String industry;

    private @Nullable String phone;

    private @Nullable String address;

    private @Nullable String website;

    @Column(columnDefinition = "TEXT")
    private @Nullable String memo;

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
        this.partyType = info.partyType() == null ? PartyType.CLIENT : info.partyType();
        this.businessNumber = normalizeBusinessNumber(info.businessNumber());
        this.industry = blankToNull(info.industry());
        this.phone = blankToNull(info.phone());
        this.address = blankToNull(info.address());
        this.website = normalizeWebsite(info.website());
        this.memo = blankToNull(info.memo());
    }

    /** 숫자 10자리를 000-00-00000 형식으로 맞춘다. */
    static @Nullable String normalizeBusinessNumber(@Nullable String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.length() != 10) {
            throw new BusinessException("사업자등록번호는 숫자 10자리여야 합니다: " + value);
        }
        return digits.substring(0, 3) + "-" + digits.substring(3, 5) + "-" + digits.substring(5);
    }

    private static @Nullable String normalizeWebsite(@Nullable String value) {
        String trimmed = blankToNull(value);
        if (trimmed == null) {
            return null;
        }
        return trimmed.matches("(?i)^https?://.*") ? trimmed : "https://" + trimmed;
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

    public PartyType getPartyType() {
        return partyType;
    }

    public @Nullable String getBusinessNumber() {
        return businessNumber;
    }

    public @Nullable String getIndustry() {
        return industry;
    }

    public @Nullable String getPhone() {
        return phone;
    }

    public @Nullable String getAddress() {
        return address;
    }

    public @Nullable String getWebsite() {
        return website;
    }

    public @Nullable String getMemo() {
        return memo;
    }

    public record PartyInfo(
            String name,
            @Nullable String ceoName,
            @Nullable String salesRepName,
            @Nullable String salesRepPhone,
            @Nullable String salesRepEmail,
            @Nullable PartyType partyType,
            @Nullable String businessNumber,
            @Nullable String industry,
            @Nullable String phone,
            @Nullable String address,
            @Nullable String website,
            @Nullable String memo
    ) {

        /** 이름·대표자·영업 담당자만으로 만드는 정보 (구분은 고객사) */
        public PartyInfo(String name, @Nullable String ceoName, @Nullable String salesRepName,
                         @Nullable String salesRepPhone, @Nullable String salesRepEmail) {
            this(name, ceoName, salesRepName, salesRepPhone, salesRepEmail, PartyType.CLIENT, null, null, null, null, null, null);
        }

    }

}
