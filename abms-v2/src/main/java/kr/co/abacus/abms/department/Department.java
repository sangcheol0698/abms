package kr.co.abacus.abms.department;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLRestriction;
import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.common.audit.Auditable;
import kr.co.abacus.abms.common.domain.BaseEntity;
import kr.co.abacus.abms.common.domain.BusinessException;

/**
 * 부서. 상위 부서를 참조해 조직 트리를 이룬다.
 */
@Entity
@Table(name = "tb_department")
@SQLRestriction("deleted = false")
public class Department extends BaseEntity implements Auditable {

    @Column(nullable = false, length = 32, unique = true)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DepartmentType type;

    private @Nullable Long parentId;

    private @Nullable Long leaderEmployeeId;

    /** 근무 사업장 (선택) */
    private @Nullable Long siteId;

    /** 부서 소개 (역할·담당 업무) */
    @Column(length = 500)
    private @Nullable String description;

    protected Department() {
    }

    private Department(String code, String name, DepartmentType type, @Nullable Long parentId) {
        this.code = requireText(code, "부서 코드");
        this.name = requireText(name, "부서명");
        this.type = Objects.requireNonNull(type);
        this.parentId = parentId;
    }

    public static Department create(String code, String name, DepartmentType type, @Nullable Long parentId) {
        return new Department(code, name, type, parentId);
    }

    public void update(String name, DepartmentType type, @Nullable Long parentId) {
        if (parentId != null && parentId.equals(getId())) {
            throw new BusinessException("자기 자신을 상위 부서로 지정할 수 없습니다.");
        }
        this.name = requireText(name, "부서명");
        this.type = Objects.requireNonNull(type);
        this.parentId = parentId;
    }

    public void describe(@Nullable String description) {
        String trimmed = description == null || description.isBlank() ? null : description.trim();
        if (trimmed != null && trimmed.length() > 500) {
            throw new BusinessException("부서 소개는 500자 이하로 입력하세요.");
        }
        this.description = trimmed;
    }

    public void relocate(@Nullable Long siteId) {
        this.siteId = siteId;
    }

    public void assignLeader(@Nullable Long leaderEmployeeId) {
        this.leaderEmployeeId = leaderEmployeeId;
    }

    private static String requireText(@Nullable String value, String field) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(field + "은(는) 필수입니다.");
        }
        return value.trim();
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public DepartmentType getType() {
        return type;
    }

    public @Nullable Long getParentId() {
        return parentId;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public @Nullable Long getSiteId() {
        return siteId;
    }

    public @Nullable Long getLeaderEmployeeId() {
        return leaderEmployeeId;
    }

    @Override
    public String auditLabel() {
        return "부서";
    }

    @Override
    public String auditName() {
        return name;
    }

}
