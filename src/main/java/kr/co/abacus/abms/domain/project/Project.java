package kr.co.abacus.abms.domain.project;

import static java.util.Objects.*;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

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
@Table(name = "tb_project", uniqueConstraints = @UniqueConstraint(name = "UK_PROJECT_CODE", columnNames = {
        "project_code"}))
public class Project extends AbstractEntity {

    @Column(name = "party_id", nullable = false)
    private Long partyId;

    @Column(name = "lead_department_id", nullable = false)
    private Long leadDepartmentId;

    @Column(name = "project_code", nullable = false, length = 50)
    private String code;

    @Column(name = "project_name", nullable = false, length = 100)
    private String name;

    @Nullable
    @Column(name = "project_description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_status", nullable = false, length = 20)
    private ProjectStatus status;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "contract_amount", nullable = false))
    private Money contractAmount;

    @Embedded
    @AttributeOverride(name = "startDate", column = @Column(name = "start_date", nullable = false))
    @AttributeOverride(name = "endDate", column = @Column(name = "end_date", nullable = false))
    private Period period;

    private Project(Long partyId, Long leadDepartmentId, String code, String name, @Nullable String description, ProjectStatus status,
                    Money contractAmount, Period period) {
        this.partyId = partyId;
        this.leadDepartmentId = leadDepartmentId;
        this.code = code;
        this.name = name;
        this.description = description;
        this.status = status;
        this.contractAmount = contractAmount;
        this.period = period;
    }

    public static Project create(ProjectCreateRequest request) {
        return new Project(
                requireNonNull(request.partyId()),
                requireNonNull(request.leadDepartmentId()),
                requireNonNull(request.code()),
                requireNonNull(request.name()),
                request.description(),
                requireNonNull(request.status()),
                Money.wons(requireNonNull(request.contractAmount())),
                new Period(requireNonNull(request.startDate()), requireNonNull(request.endDate())));
    }

    public void update(ProjectUpdateRequest request) {
        this.partyId = requireNonNull(request.partyId());
        this.leadDepartmentId = requireNonNull(request.leadDepartmentId());
        this.name = requireNonNull(request.name());
        this.description = request.description();
        this.status = requireNonNull(request.status());
        this.contractAmount = Money.wons(requireNonNull(request.contractAmount()));
        this.period = new Period(requireNonNull(request.startDate()), request.endDate());
    }

    public void complete() {
        this.status = ProjectStatus.COMPLETED;
    }

    public void cancel() {
        this.status = ProjectStatus.CANCELLED;
    }

}
