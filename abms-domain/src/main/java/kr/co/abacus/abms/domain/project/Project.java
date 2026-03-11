package kr.co.abacus.abms.domain.project;

import static java.util.Objects.*;

import java.time.LocalDate;

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
    @AttributeOverride(name = "endDate", column = @Column(name = "end_date"))
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

    public static Project create(
            Long partyId,
            Long leadDepartmentId,
            String code,
            String name,
            @Nullable String description,
            ProjectStatus status,
            Long contractAmount,
            LocalDate startDate,
            @Nullable LocalDate endDate) {
        return new Project(
                requireNonNull(partyId),
                requireNonNull(leadDepartmentId),
                requireNonNull(code),
                requireNonNull(name),
                description,
                requireNonNull(status),
                Money.wons(requireNonNull(contractAmount)),
                new Period(requireNonNull(startDate), endDate));
    }

    public void update(
            Long partyId,
            Long leadDepartmentId,
            String name,
            @Nullable String description,
            ProjectStatus status,
            Long contractAmount,
            LocalDate startDate,
            @Nullable LocalDate endDate) {
        this.partyId = requireNonNull(partyId);
        this.leadDepartmentId = requireNonNull(leadDepartmentId);
        this.name = requireNonNull(name);
        this.description = description;
        this.status = requireNonNull(status);
        this.contractAmount = Money.wons(requireNonNull(contractAmount));
        this.period = new Period(requireNonNull(startDate), endDate);
    }

    public void complete() {
        this.status = ProjectStatus.COMPLETED;
    }

    public void cancel() {
        this.status = ProjectStatus.CANCELLED;
    }

}
