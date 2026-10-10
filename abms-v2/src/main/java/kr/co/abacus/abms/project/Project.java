package kr.co.abacus.abms.project;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;
import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.audit.Auditable;
import kr.co.abacus.abms.common.domain.BaseEntity;
import kr.co.abacus.abms.common.domain.BusinessException;
import kr.co.abacus.abms.common.domain.Location;
import kr.co.abacus.abms.common.domain.Money;
import kr.co.abacus.abms.common.domain.Period;
import kr.co.abacus.abms.security.ScopedProject;

/**
 * 프로젝트 Aggregate Root. 손익은 주관 부서({@link #leadDepartmentId})에 귀속된다.
 */
@Entity
@Table(name = "tb_project")
@SQLRestriction("deleted = false")
public class Project extends BaseEntity implements Auditable, ScopedProject {

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

    /** 수행 장소 구분 (미정이면 null) */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private @Nullable WorkPlace workPlace;

    /** 수행 장소 주소. 별도 장소이거나, 고객사 상주인데 협력사 주소와 다를 때 쓴다. */
    @Embedded
    @AttributeOverride(name = "zipCode", column = @Column(name = "work_zip_code", length = 10))
    @AttributeOverride(name = "address", column = @Column(name = "work_address"))
    @AttributeOverride(name = "addressDetail", column = @Column(name = "work_address_detail", length = 100))
    @AttributeOverride(name = "latitude", column = @Column(name = "work_latitude", precision = 10, scale = 7))
    @AttributeOverride(name = "longitude", column = @Column(name = "work_longitude", precision = 10, scale = 7))
    private @Nullable Location workLocation;

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

    /** 수행 장소 지정. 원격·자사는 주소를 두지 않는다. (자사는 주관 부서 사업장 주소를 쓴다) */
    public void assignWorkPlace(@Nullable WorkPlace workPlace, @Nullable Location location) {
        this.workPlace = workPlace;
        boolean addressAllowed = workPlace == WorkPlace.OTHER || workPlace == WorkPlace.CLIENT_SITE;
        this.workLocation = !addressAllowed || location == null || location.isEmpty() ? null : location;
    }

    public @Nullable WorkPlace getWorkPlace() {
        return workPlace;
    }

    /** 직접 입력한 수행 장소 주소. 없으면 빈 위치. */
    public Location getWorkLocation() {
        return workLocation == null ? Location.EMPTY : workLocation;
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

    /**
     * 진행 기준(관리) 매출: 계약금액을 프로젝트 기간에 일 단위로 고르게 배분한 해당 월의 몫.
     * 누적 배분액의 차이로 구하므로 월별 금액을 모두 더하면 계약금액과 정확히 같다.
     */
    public Money managedRevenue(YearMonth month) {
        return recognizedThrough(month.atEndOfMonth()).minus(recognizedThrough(month.minusMonths(1).atEndOfMonth()));
    }

    private Money recognizedThrough(LocalDate date) {
        LocalDate start = period.startDate();
        LocalDate end = Objects.requireNonNull(period.endDate(), "프로젝트 종료일은 필수입니다.");
        if (date.isBefore(start)) {
            return Money.ZERO;
        }
        if (!date.isBefore(end)) {
            return contractAmount;
        }
        long elapsed = ChronoUnit.DAYS.between(start, date) + 1;
        long total = ChronoUnit.DAYS.between(start, end) + 1;
        return new Money(contractAmount.amount().multiply(BigDecimal.valueOf(elapsed))
                .divide(BigDecimal.valueOf(total), 0, RoundingMode.HALF_UP));
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

    @Override
    public String auditLabel() {
        return "프로젝트";
    }

    @Override
    public String auditName() {
        return code + " " + name;
    }

}
