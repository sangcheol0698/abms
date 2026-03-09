package kr.co.abacus.abms.application.employee.authorization;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jspecify.annotations.Nullable;

public record EmployeeReadScope(
        boolean allAllowed,
        Set<Long> allowedDepartmentIds,
        @Nullable Long selfEmployeeId,
        @Nullable Long selfDepartmentId
) {

    public EmployeeReadScope {
        allowedDepartmentIds = Set.copyOf(allowedDepartmentIds);
    }

    public static EmployeeReadScope all() {
        return new EmployeeReadScope(true, Set.of(), null, null);
    }

    public static EmployeeReadScope none() {
        return new EmployeeReadScope(false, Set.of(), null, null);
    }

    public boolean canRead(@Nullable Long employeeId, @Nullable Long departmentId) {
        if (allAllowed) {
            return true;
        }
        if (selfEmployeeId != null && Objects.equals(selfEmployeeId, employeeId)) {
            return true;
        }
        return departmentId != null && allowedDepartmentIds.contains(departmentId);
    }

    public EmployeeReadScope limitToRequestedDepartments(@Nullable List<Long> requestedDepartmentIds) {
        if (allAllowed || requestedDepartmentIds == null || requestedDepartmentIds.isEmpty()) {
            return this;
        }

        Set<Long> requestedIds = new LinkedHashSet<>(requestedDepartmentIds);
        Set<Long> intersectedDepartmentIds = allowedDepartmentIds.stream()
                .filter(requestedIds::contains)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        boolean selfAllowed = selfEmployeeId != null
                && selfDepartmentId != null
                && requestedIds.contains(selfDepartmentId);

        return new EmployeeReadScope(
                false,
                intersectedDepartmentIds,
                selfAllowed ? selfEmployeeId : null,
                selfAllowed ? selfDepartmentId : null
        );
    }

    public boolean usesDepartmentScope() {
        return !allowedDepartmentIds.isEmpty();
    }
}
