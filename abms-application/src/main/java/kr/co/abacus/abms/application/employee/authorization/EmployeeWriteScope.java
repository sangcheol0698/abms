package kr.co.abacus.abms.application.employee.authorization;

import java.util.Set;

import org.jspecify.annotations.Nullable;

public record EmployeeWriteScope(
        boolean allAllowed,
        Set<Long> allowedDepartmentIds,
        @Nullable Long selfEmployeeId,
        @Nullable Long selfDepartmentId
) {

    public EmployeeWriteScope {
        allowedDepartmentIds = Set.copyOf(allowedDepartmentIds);
    }

    public static EmployeeWriteScope all() {
        return new EmployeeWriteScope(true, Set.of(), null, null);
    }

    public static EmployeeWriteScope none() {
        return new EmployeeWriteScope(false, Set.of(), null, null);
    }

    public boolean canCreateIn(@Nullable Long departmentId) {
        if (allAllowed) {
            return true;
        }
        return departmentId != null && allowedDepartmentIds.contains(departmentId);
    }

    public boolean canManageTarget(@Nullable Long departmentId) {
        if (allAllowed) {
            return true;
        }
        return departmentId != null && allowedDepartmentIds.contains(departmentId);
    }

    public boolean canUpdateOwnProfile(@Nullable Long employeeId) {
        return selfEmployeeId != null && selfEmployeeId.equals(employeeId);
    }

    public boolean isSelfOnly() {
        return !allAllowed && allowedDepartmentIds.isEmpty() && selfEmployeeId != null;
    }
}
