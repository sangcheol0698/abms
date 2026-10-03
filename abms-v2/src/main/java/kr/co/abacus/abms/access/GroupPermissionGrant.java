package kr.co.abacus.abms.access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import kr.co.abacus.abms.common.domain.BaseEntity;

/**
 * 권한 그룹에 부여된 (권한, 범위).
 */
@Entity
@Table(name = "tb_group_permission_grant")
public class GroupPermissionGrant extends BaseEntity {

    @Column(nullable = false)
    private Long permissionGroupId;

    @Column(nullable = false)
    private Long permissionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PermissionScope scope;

    protected GroupPermissionGrant() {
    }

    public static GroupPermissionGrant of(Long permissionGroupId, Long permissionId, PermissionScope scope) {
        GroupPermissionGrant grant = new GroupPermissionGrant();
        grant.permissionGroupId = permissionGroupId;
        grant.permissionId = permissionId;
        grant.scope = scope;
        return grant;
    }

    public Long getPermissionGroupId() {
        return permissionGroupId;
    }

    public Long getPermissionId() {
        return permissionId;
    }

    public PermissionScope getScope() {
        return scope;
    }

}
