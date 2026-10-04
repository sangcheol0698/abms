package kr.co.abacus.abms.site;

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

/**
 * 사업장(본사·지사·연구소 등). 부서가 근무하는 장소로 연결된다.
 */
@Entity
@Table(name = "tb_site")
@SQLRestriction("deleted = false")
public class Site extends BaseEntity implements Auditable {

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SiteType siteType = SiteType.OFFICE;

    @Column(length = 20)
    private @Nullable String phone;

    /** 소재지 (선택) */
    @Embedded
    private @Nullable Location location;

    @Column(columnDefinition = "TEXT")
    private @Nullable String memo;

    protected Site() {
    }

    public static Site create(SiteInfo info) {
        Site site = new Site();
        site.apply(info);
        return site;
    }

    public void update(SiteInfo info) {
        apply(info);
    }

    private void apply(SiteInfo info) {
        String trimmed = blankToNull(info.name());
        if (trimmed == null) {
            throw new BusinessException("사업장명은 필수입니다.");
        }
        if (trimmed.length() > 50) {
            throw new BusinessException("사업장명은 50자 이하로 입력하세요.");
        }
        this.name = trimmed;
        this.siteType = info.siteType() == null ? SiteType.OFFICE : info.siteType();
        this.phone = blankToNull(info.phone());
        this.location = info.location() == null || info.location().isEmpty() ? null : info.location();
        this.memo = blankToNull(info.memo());
    }

    private static @Nullable String blankToNull(@Nullable String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public String getName() {
        return name;
    }

    public SiteType getSiteType() {
        return siteType;
    }

    public @Nullable String getPhone() {
        return phone;
    }

    /** 소재지. 주소가 없으면 빈 위치를 돌려준다. */
    public Location getLocation() {
        return location == null ? Location.EMPTY : location;
    }

    public @Nullable String getMemo() {
        return memo;
    }

    public record SiteInfo(@Nullable String name, @Nullable SiteType siteType, @Nullable String phone,
                           @Nullable Location location, @Nullable String memo) {

        public SiteInfo withLocation(Location location) {
            return new SiteInfo(name, siteType, phone, location, memo);
        }

    }

    @Override
    public String auditLabel() {
        return "사업장";
    }

    @Override
    public String auditName() {
        return name;
    }

}
