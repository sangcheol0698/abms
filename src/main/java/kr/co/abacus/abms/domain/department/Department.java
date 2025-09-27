package kr.co.abacus.abms.domain.department;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

import kr.co.abacus.abms.domain.AbstractEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@NaturalIdCache
@Entity
@Table(name = "department", uniqueConstraints = {
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
    @Column(name = "leader_employee_id", length = 32)
    private UUID leaderEmployeeId;

    @Nullable
    @JoinColumn(name = "department_parent_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Department parent;

    @OneToMany(mappedBy = "parent", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Department> children = new ArrayList<>();

    public static Department createRoot(DepartmentCreateRequest request) {
        Department department = new Department();

        department.code = requireNonNull(request.code());
        department.name = requireNonNull(request.name());
        department.type = requireNonNull(request.type());
        department.leaderEmployeeId = request.leaderEmployeeId();
        department.parent = null;

        return department;
    }

    public static Department create(DepartmentCreateRequest request, Department parent) {
        requireNonNull(parent, "상위 부서는 필수입니다");

        Department department = new Department();

        department.code = requireNonNull(request.code());
        department.name = requireNonNull(request.name());
        department.type = requireNonNull(request.type());
        department.leaderEmployeeId = request.leaderEmployeeId();

        department.setParent(parent);

        return department;
    }

    private void setParent(Department parent) {
        if (this.parent != null) {
            this.parent.children.remove(this);
        }

        this.parent = parent;
        if (!parent.children.contains(this)) {
            parent.children.add(this);
        }
    }

    public boolean isRoot() {
        return parent == null;
    }

}
