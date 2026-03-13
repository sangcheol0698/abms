package kr.co.abacus.abms.application.project.authorization;

import java.util.Set;

import org.jspecify.annotations.Nullable;

public record ProjectWriteScope(
        boolean allAllowed,
        Set<Long> allowedLeadDepartmentIds
) {

    public ProjectWriteScope {
        allowedLeadDepartmentIds = Set.copyOf(allowedLeadDepartmentIds);
    }

    public static ProjectWriteScope all() {
        return new ProjectWriteScope(true, Set.of());
    }

    public static ProjectWriteScope none() {
        return new ProjectWriteScope(false, Set.of());
    }

    public boolean canManage(@Nullable Long leadDepartmentId) {
        if (allAllowed) {
            return true;
        }

        return leadDepartmentId != null && allowedLeadDepartmentIds.contains(leadDepartmentId);
    }
}
