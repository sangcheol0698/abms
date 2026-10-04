package kr.co.abacus.abms.employee;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

/**
 * 직원 목록 검색 조건.
 */
public record EmployeeSearch(
        @Nullable String keyword,
        @Nullable Long departmentId,
        @Nullable EmployeeStatus status,
        @Nullable EmployeeType type,
        @Nullable EmployeePosition position,
        boolean deleted
) {

    public static EmployeeSearch empty() {
        return new EmployeeSearch(null, null, null, null, null, false);
    }

    public Specification<Employee> toSpecification(@Nullable Collection<Long> departmentIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), deleted));
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(cb.like(cb.lower(root.get("name")), like), cb.like(cb.lower(root.get("email")), like),
                        cb.like(cb.lower(root.get("skills")), like)));
            }
            if (departmentIds != null) {
                predicates.add(root.get("departmentId").in(departmentIds));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (position != null) {
                predicates.add(cb.equal(root.get("position"), position));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

}
