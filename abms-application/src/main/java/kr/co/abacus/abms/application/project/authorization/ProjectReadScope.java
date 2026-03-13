package kr.co.abacus.abms.application.project.authorization;

import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.project.dto.ProjectSearchCondition;

public record ProjectReadScope(
        boolean allAllowed,
        Set<Long> allowedProjectIds,
        Set<Long> allowedLeadDepartmentIds
) {

    public ProjectReadScope {
        allowedProjectIds = Set.copyOf(allowedProjectIds);
        allowedLeadDepartmentIds = Set.copyOf(allowedLeadDepartmentIds);
    }

    public static ProjectReadScope all() {
        return new ProjectReadScope(true, Set.of(), Set.of());
    }

    public static ProjectReadScope none() {
        return new ProjectReadScope(false, Set.of(), Set.of());
    }

    public boolean canRead(Long projectId, @Nullable Long leadDepartmentId) {
        if (allAllowed) {
            return true;
        }

        if (allowedProjectIds.contains(projectId)) {
            return true;
        }

        return leadDepartmentId != null && allowedLeadDepartmentIds.contains(leadDepartmentId);
    }

    public ProjectSearchCondition apply(ProjectSearchCondition condition) {
        if (allAllowed) {
            return condition;
        }

        return condition.withAccess(
                List.copyOf(allowedProjectIds),
                List.copyOf(allowedLeadDepartmentIds)
        );
    }
}
