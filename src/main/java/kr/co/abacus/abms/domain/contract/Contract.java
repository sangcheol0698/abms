package kr.co.abacus.abms.domain.contract;

import static java.util.Objects.*;

import java.time.LocalDate;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.jspecify.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.AbstractEntity;
import kr.co.abacus.abms.domain.shared.Money;
import kr.co.abacus.abms.domain.shared.Period;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "contract")
public class Contract extends AbstractEntity {

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false, length = 10)
    private ContractType contractType;

    @Nullable
    @Column(name = "contract_date")
    private LocalDate contractDate;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "contract_amount", nullable = false))
    private Money contractAmount;

    @Embedded
    @AttributeOverride(name = "startDate", column = @Column(name = "start_date", nullable = false))
    @AttributeOverride(name = "endDate", column = @Column(name = "end_date"))
    private Period period;

    public static Contract createInitial(ContractCreateRequest request) {
        Contract contract = new Contract();

        contract.projectId = requireNonNull(request.projectId());
        contract.version = 1;
        contract.contractType = ContractType.INITIAL;
        contract.contractDate = request.contractDate();
        contract.contractAmount = Money.wons(requireNonNull(request.contractAmount()));
        contract.period = new Period(requireNonNull(request.startDate()), request.endDate());

        return contract;
    }

    public static Contract renew(Contract previousContract, ContractCreateRequest request) {
        requireNonNull(previousContract, "이전 계약이 필요합니다");

        Contract contract = new Contract();

        contract.projectId = previousContract.projectId;
        contract.version = previousContract.version + 1;
        contract.contractType = ContractType.AMENDMENT;
        contract.contractDate = request.contractDate();
        contract.contractAmount = Money.wons(requireNonNull(request.contractAmount()));
        contract.period = new Period(requireNonNull(request.startDate()), request.endDate());

        return contract;
    }

    public void update(ContractUpdateRequest request) {
        this.contractDate = request.contractDate();
        this.contractAmount = Money.wons(requireNonNull(request.contractAmount()));
        this.period = new Period(requireNonNull(request.startDate()), request.endDate());
    }

}
