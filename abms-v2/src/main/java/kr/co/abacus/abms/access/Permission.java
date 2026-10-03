package kr.co.abacus.abms.access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;

import kr.co.abacus.abms.common.domain.BaseEntity;

/**
 * 권한 정의 (시드 데이터로만 관리).
 */
@Entity
@Table(name = "tb_permission")
@SQLRestriction("deleted = false")
public class Permission extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String description;

    protected Permission() {
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
