package kr.co.abacus.abms.domain.permissiongroup;

import static java.util.Objects.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.AbstractEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_permission_group")
public class PermissionGroup extends AbstractEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "group_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PermissionGroupType groupType;

    public static PermissionGroup create(String name, String description, PermissionGroupType groupType) {
        PermissionGroup permissionGroup = new PermissionGroup();

        permissionGroup.name = requireNonNull(name);
        permissionGroup.description = requireNonNull(description);
        permissionGroup.groupType = requireNonNull(groupType);

        return permissionGroup;
    }

}
