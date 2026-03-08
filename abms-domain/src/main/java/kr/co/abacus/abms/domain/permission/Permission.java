package kr.co.abacus.abms.domain.permission;

import static java.util.Objects.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.AbstractEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_permission",
        uniqueConstraints = @UniqueConstraint(name = "UK_PERMISSION_CODE", columnNames = "code")
)
public class Permission extends AbstractEntity {

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    public static Permission create(String code, String name, String description) {
        Permission permission = new Permission();

        permission.code = requireNonNull(code);
        permission.name = requireNonNull(name);
        permission.description = requireNonNull(description);

        return permission;
    }

}
