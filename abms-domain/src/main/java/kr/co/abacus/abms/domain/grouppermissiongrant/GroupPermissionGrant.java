package kr.co.abacus.abms.domain.grouppermissiongrant;

import static java.util.Objects.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.AbstractEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_group_permission_grant",
        uniqueConstraints = @UniqueConstraint(
                name = "UK_GROUP_PERMISSION_GRANT",
                columnNames = {"permission_group_id", "permission_id", "scope"}
        )
)
public class GroupPermissionGrant extends AbstractEntity {

    @Column(name = "permission_group_id", nullable = false)
    private Long permissionGroupId;

    @Column(name = "permission_id", nullable = false)
    private Long permissionId;

    @Column(name = "scope", nullable = false)
    @Enumerated(EnumType.STRING)
    private PermissionScope scope;

    public static GroupPermissionGrant create(Long permissionGroupId, Long permissionId, PermissionScope scope) {
        GroupPermissionGrant grant = new GroupPermissionGrant();

        grant.permissionGroupId = requireNonNull(permissionGroupId);
        grant.permissionId = requireNonNull(permissionId);
        grant.scope = requireNonNull(scope);

        return grant;
    }

}
