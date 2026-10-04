package kr.co.abacus.abms.party;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;
import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.audit.Auditable;
import kr.co.abacus.abms.common.audit.Auditable.AuditRef;
import kr.co.abacus.abms.common.domain.BaseEntity;
import kr.co.abacus.abms.common.domain.BusinessException;

/**
 * 협력사 담당자. 역할(영업·계약·청구·기술)별로 여러 명을 두고, 그중 한 명을 대표 담당자로 지정한다.
 */
@Entity
@Table(name = "tb_party_contact")
@SQLRestriction("deleted = false")
public class PartyContact extends BaseEntity implements Auditable {

    @Column(nullable = false)
    private Long partyId;

    @Column(nullable = false, length = 30)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContactRole role;

    @Column(length = 30)
    private @Nullable String title;

    @Column(length = 20)
    private @Nullable String phone;

    @Column(length = 100)
    private @Nullable String email;

    @Column(length = 500)
    private @Nullable String memo;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    protected PartyContact() {
    }

    public static PartyContact create(Long partyId, ContactInfo info) {
        PartyContact contact = new PartyContact();
        contact.partyId = Objects.requireNonNull(partyId);
        contact.apply(info);
        return contact;
    }

    public void update(ContactInfo info) {
        apply(info);
    }

    void markPrimary(boolean primary) {
        this.primary = primary;
    }

    private void apply(ContactInfo info) {
        String trimmed = blankToNull(info.name());
        if (trimmed == null) {
            throw new BusinessException("담당자 이름은 필수입니다.");
        }
        this.name = trimmed;
        this.role = info.role() == null ? ContactRole.ETC : info.role();
        this.title = blankToNull(info.title());
        this.phone = blankToNull(info.phone());
        this.email = blankToNull(info.email());
        this.memo = blankToNull(info.memo());
    }

    private static @Nullable String blankToNull(@Nullable String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public Long getPartyId() {
        return partyId;
    }

    public String getName() {
        return name;
    }

    public ContactRole getRole() {
        return role;
    }

    public @Nullable String getTitle() {
        return title;
    }

    public @Nullable String getPhone() {
        return phone;
    }

    public @Nullable String getEmail() {
        return email;
    }

    public @Nullable String getMemo() {
        return memo;
    }

    public boolean isPrimary() {
        return primary;
    }

    @Override
    public String auditLabel() {
        return "협력사 담당자";
    }

    @Override
    public String auditName() {
        return name + " (" + role.label() + ")";
    }

    @Override
    public AuditRef auditParent() {
        return new AuditRef("Party", partyId);
    }

    public record ContactInfo(@Nullable String name, @Nullable ContactRole role, @Nullable String title, @Nullable String phone,
                              @Nullable String email, @Nullable String memo, boolean primary) {
    }

}
