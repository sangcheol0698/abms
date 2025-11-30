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
@Table(name = "party")
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

        party.name = requireNonNull(request.name());
        party.ceoName = request.ceoName();
        party.salesRepName = request.salesRepName();
        party.salesRepPhone = request.salesRepPhone();
        party.salesRepEmail = request.salesRepEmail();

        return party;
    }

    public void update(PartyUpdateRequest request) {
        this.name = requireNonNull(request.name());
        this.ceoName = request.ceoName();
        this.salesRepName = request.salesRepName();
        this.salesRepPhone = request.salesRepPhone();
        this.salesRepEmail = request.salesRepEmail();
    }

}
