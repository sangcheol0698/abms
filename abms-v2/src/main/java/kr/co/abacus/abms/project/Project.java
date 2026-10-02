package kr.co.abacus.abms.project;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;
import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.domain.BaseEntity;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.common.domain.Period;

/**
 * 프로젝트 Aggregate Root. 손익은 주관 부서({@link #leadDepartmentId})에 귀속된다.
 */
@Entity
@Table(name = "tb_project")
@SQLRestriction("deleted = false")
public class Project extends BaseEntity {

    @Column(nullable = false)
    private Long partyId;

    @Column(nullable = false)
    private Long leadDepartmentId;

    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private @Nullable String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatus status;

    @Column(nullable = false)
    private Money contractAmount;

    @Embedded
    private Period period;

    protected Project() {
    }

    public static Project create(String code, ProjectInfo info) {
        if (code == null || code.isBlank()) {
            throw new BusinessException("프로젝트 코드는 필수입니다.");
        }
        Project project = new Project();
        project.code = code.trim();
        project.apply(info);
        return project;
    }

    public void update(ProjectInfo info) {
        apply(info);
    }

    private void apply(ProjectInfo info) {
        if (info.name() == null || info.name().isBlank()) {
            throw new BusinessException("프로젝트명은 필수입니다.");
        }
        if (info.contractAmount().isNegative()) {
            throw new BusinessException("계약금액은 음수일 수 없습니다.");
        }
        if (info.period().endDate() == null) {
            throw new BusinessException("프로젝트 종료일은 필수입니다.");
        }
        this.partyId = Objects.requireNonNull(info.partyId(), "협력사는 필수입니다.");
        this.leadDepartmentId = Objects.requireNonNull(info.leadDepartmentId(), "주관 부서는 필수입니다.");
        this.name = info.name().trim();
        this.description = info.description() == null || info.description().isBlank() ? null : info.description().trim();
        this.status = Objects.requireNonNull(info.status());
        this.contractAmount = info.contractAmount();
        this.period = info.period();
    }

    public void complete() {
        if (status == ProjectStatus.CANCELLED) {
            throw new BusinessException("취소된 프로젝트는 완료 처리할 수 없습니다.");
        }
        this.status = ProjectStatus.COMPLETED;
    }

    public void cancel() {
        if (status == ProjectStatus.COMPLETED) {
            throw new BusinessException("완료된 프로젝트는 취소할 수 없습니다.");
        }
        this.status = ProjectStatus.CANCELLED;
    }

    public Long getPartyId() {
        return partyId;
    }

    public Long getLeadDepartmentId() {
        return leadDepartmentId;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public Money getContractAmount() {
        return contractAmount;
    }

    public Period getPeriod() {
        return period;
    }

    public record ProjectInfo(
            Long partyId,
            Long leadDepartmentId,
            String name,
            @Nullable String description,
            ProjectStatus status,
            Money contractAmount,
            Period period
    ) {
    }

}
