package kr.co.abacus.abms.project;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import kr.co.abacus.abms.security.DataScope;

/**
 * 프로젝트 목록 검색 조건.
 */
public record ProjectSearch(
        @Nullable String keyword,
        @Nullable ProjectStatus status,
        @Nullable Long partyId,
        @Nullable Long leadDepartmentId,
        @Nullable LocalDate activeOn
) {

    public static ProjectSearch empty() {
        return new ProjectSearch(null, null, null, null, null);
    }

    public Specification<Project> toSpecification(DataScope scope) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (!scope.all()) {
                List<Predicate> scopePredicates = new ArrayList<>();
                if (!scope.departmentIds().isEmpty()) {
                    scopePredicates.add(root.get("leadDepartmentId").in(scope.departmentIds()));
                }
                if (!scope.projectIds().isEmpty()) {
                    scopePredicates.add(root.get("id").in(scope.projectIds()));
                }
                predicates.add(scopePredicates.isEmpty() ? cb.disjunction() : cb.or(scopePredicates.toArray(Predicate[]::new)));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(cb.like(cb.lower(root.get("name")), like), cb.like(cb.lower(root.get("code")), like)));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (partyId != null) {
                predicates.add(cb.equal(root.get("partyId"), partyId));
            }
            if (leadDepartmentId != null) {
                predicates.add(cb.equal(root.get("leadDepartmentId"), leadDepartmentId));
            }
            if (activeOn != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("period").get("startDate"), activeOn));
                predicates.add(cb.or(cb.isNull(root.get("period").get("endDate")),
                        cb.greaterThanOrEqualTo(root.get("period").get("endDate"), activeOn)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

}
