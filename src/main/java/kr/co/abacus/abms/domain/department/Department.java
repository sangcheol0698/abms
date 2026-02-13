package kr.co.abacus.abms.domain.department;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.jspecify.annotations.Nullable;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.AbstractEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@NaturalIdCache
@Entity
@Table(name = "tb_department", uniqueConstraints = {
        @UniqueConstraint(name = "UK_DEPARTMENT_CODE", columnNames = {"department_code"})
})
public class Department extends AbstractEntity {

    @NaturalId
    @Column(name = "department_code", nullable = false, length = 32)
    private String code;

    @Column(name = "department_name", nullable = false, length = 30)
    private String name;

    @Column(name = "department_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private DepartmentType type;

    @Nullable
    @Column(name = "leader_id", length = 32)
    private Long leaderEmployeeId;

    @Nullable
    @JoinColumn(name = "parent_department_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Department parent;

    @OneToMany(mappedBy = "parent")
    private List<Department> children = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Department(String code, String name, DepartmentType type, @Nullable Long leaderEmployeeId,
                       @Nullable Department parent) {
        this.code = requireNonNull(code);
        this.name = requireNonNull(name);
        this.type = requireNonNull(type);
        this.leaderEmployeeId = leaderEmployeeId;

        setParent(parent);
    }

    public static Department create(String code, String name, DepartmentType type, @Nullable Long leaderEmployeeId,
                                    @Nullable Department parent) {
        return Department.builder()
                .code(code)
                .name(name)
                .type(type)
                .leaderEmployeeId(leaderEmployeeId)
                .parent(parent)
                .build();
    }

    private void setParent(@Nullable Department parent) {
        if (this.parent != null) {
            this.parent.children.remove(this);
        }

        this.parent = parent;
        if (parent != null && !parent.children.contains(this)) {
            parent.children.add(this);
        }
    }

    public void assignLeader(Long leaderEmployeeId) {
        this.leaderEmployeeId = leaderEmployeeId;
    }

}
