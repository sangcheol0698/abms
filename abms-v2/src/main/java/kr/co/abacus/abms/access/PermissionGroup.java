package kr.co.abacus.abms.access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;

import kr.co.abacus.abms.common.audit.Auditable;
import kr.co.abacus.abms.common.domain.BaseEntity;
import kr.co.abacus.abms.common.domain.BusinessException;

/**
 * 권한 그룹. 시스템 그룹은 이름/권한 구성을 바꿀 수 없고 계정 할당만 가능하다.
 */
@Entity
@Table(name = "tb_permission_group")
@SQLRestriction("deleted = false")
public class PermissionGroup extends BaseEntity implements Auditable {

    public static final long DEFAULT_GROUP_ID = 1L;
    public static final long ADMIN_GROUP_ID = 2L;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PermissionGroupType groupType;

    protected PermissionGroup() {
    }

    public static PermissionGroup custom(String name, String description) {
        PermissionGroup group = new PermissionGroup();
        group.groupType = PermissionGroupType.CUSTOM;
        group.rename(name, description);
        return group;
    }

    public void update(String name, String description) {
        requireCustom();
        rename(name, description);
    }

    private void rename(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("그룹명은 필수입니다.");
        }
        this.name = name.trim();
        this.description = description == null ? "" : description.trim();
    }

    public void requireCustom() {
        if (isSystem()) {
            throw new BusinessException("시스템 권한 그룹은 변경할 수 없습니다.");
        }
    }

    public boolean isSystem() {
        return groupType == PermissionGroupType.SYSTEM;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public PermissionGroupType getGroupType() {
        return groupType;
    }

    @Override
    public String auditLabel() {
        return "권한 그룹";
    }

    @Override
    public String auditName() {
        return name;
    }

}
