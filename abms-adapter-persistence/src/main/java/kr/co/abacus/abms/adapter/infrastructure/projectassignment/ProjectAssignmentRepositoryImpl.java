package kr.co.abacus.abms.adapter.infrastructure.projectassignment;

import static kr.co.abacus.abms.domain.department.QDepartment.*;
import static kr.co.abacus.abms.domain.employee.QEmployee.*;
import static kr.co.abacus.abms.domain.party.QParty.*;
import static kr.co.abacus.abms.domain.project.QProject.*;
import static kr.co.abacus.abms.domain.projectassignment.QProjectAssignment.*;
import static org.springframework.util.StringUtils.*;

import java.time.LocalDate;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.projectassignment.dto.EmployeeProjectItem;
import kr.co.abacus.abms.application.projectassignment.dto.EmployeeProjectSearchCondition;
import kr.co.abacus.abms.application.projectassignment.dto.ProjectAssignmentItem;
import kr.co.abacus.abms.application.projectassignment.dto.ProjectAssignmentSearchCondition;
import kr.co.abacus.abms.application.projectassignment.dto.ProjectAssignmentStatus;
import kr.co.abacus.abms.application.projectassignment.outbound.CustomProjectAssignmentRepository;
import kr.co.abacus.abms.domain.project.ProjectStatus;
import kr.co.abacus.abms.domain.projectassignment.AssignmentRole;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ProjectAssignmentRepositoryImpl implements CustomProjectAssignmentRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<EmployeeProjectItem> searchEmployeeProjects(EmployeeProjectSearchCondition condition, Pageable pageable) {
        return searchEmployeeProjects(condition, null, pageable);
    }

    @Override
    public Page<EmployeeProjectItem> searchEmployeeProjects(
            EmployeeProjectSearchCondition condition,
            CurrentActor actor,
            Pageable pageable
    ) {
        LocalDate today = LocalDate.now();

        List<EmployeeProjectItem> content = queryFactory
                .select(Projections.constructor(EmployeeProjectItem.class,
                        project.id,
                        project.code,
                        project.name,
                        project.partyId,
                        projectAssignment.role.stringValue(),
                        projectAssignment.period.startDate,
                        projectAssignment.period.endDate,
                        new CaseBuilder()
                                .when(projectAssignment.period.startDate.gt(today)).then(ProjectAssignmentStatus.SCHEDULED.name())
                                .when(projectAssignment.period.endDate.isNull()
                                        .or(projectAssignment.period.endDate.goe(today))).then(ProjectAssignmentStatus.CURRENT.name())
                                .otherwise(ProjectAssignmentStatus.ENDED.name()),
                        project.status.stringValue(),
                        new CaseBuilder()
                                .when(project.status.eq(ProjectStatus.SCHEDULED)).then(ProjectStatus.SCHEDULED.getDescription())
                                .when(project.status.eq(ProjectStatus.IN_PROGRESS)).then(ProjectStatus.IN_PROGRESS.getDescription())
                                .when(project.status.eq(ProjectStatus.COMPLETED)).then(ProjectStatus.COMPLETED.getDescription())
                                .when(project.status.eq(ProjectStatus.ON_HOLD)).then(ProjectStatus.ON_HOLD.getDescription())
                                .otherwise(ProjectStatus.CANCELLED.getDescription()),
                        project.leadDepartmentId,
                        department.name,
                        party.name
                ))
                .from(projectAssignment)
                .join(project).on(projectAssignment.projectId.eq(project.id))
                .join(party).on(project.partyId.eq(party.id))
                .leftJoin(department).on(project.leadDepartmentId.eq(department.id))
                .where(
                        projectAssignment.employeeId.eq(condition.employeeId()),
                        containsProjectNameOrCode(condition.name()),
                        inAssignmentStatuses(condition.assignmentStatuses(), today),
                        inProjectStatuses(condition.projectStatuses()),
                        inAccessibleScope(actor),
                        projectAssignment.deleted.isFalse(),
                        party.deleted.isFalse(),
                        project.deleted.isFalse())
                .orderBy(projectAssignment.period.startDate.desc(), projectAssignment.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(projectAssignment.count())
                .from(projectAssignment)
                .join(project).on(projectAssignment.projectId.eq(project.id))
                .join(party).on(project.partyId.eq(party.id))
                .where(
                        projectAssignment.employeeId.eq(condition.employeeId()),
                        containsProjectNameOrCode(condition.name()),
                        inAssignmentStatuses(condition.assignmentStatuses(), today),
                        inProjectStatuses(condition.projectStatuses()),
                        inAccessibleScope(actor),
                        projectAssignment.deleted.isFalse(),
                        party.deleted.isFalse(),
                        project.deleted.isFalse());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<ProjectAssignmentItem> findProjectAssignments(Long projectId) {
        LocalDate today = LocalDate.now();

        return queryFactory
                .select(Projections.constructor(ProjectAssignmentItem.class,
                        projectAssignment.id,
                        projectAssignment.projectId,
                        projectAssignment.employeeId,
                        employee.name,
                        department.id,
                        department.name,
                        projectAssignment.role.stringValue(),
                        projectAssignment.period.startDate,
                        projectAssignment.period.endDate,
                        assignmentStatusExpression(today)
                ))
                .from(projectAssignment)
                .leftJoin(employee).on(projectAssignment.employeeId.eq(employee.id))
                .leftJoin(department).on(employee.departmentId.eq(department.id))
                .where(
                        projectAssignment.projectId.eq(projectId),
                        projectAssignment.deleted.isFalse()
                )
                .orderBy(projectAssignment.period.startDate.desc(), projectAssignment.id.desc())
                .fetch();
    }

    @Override
    public Page<ProjectAssignmentItem> searchProjectAssignments(ProjectAssignmentSearchCondition condition, Pageable pageable) {
        LocalDate today = LocalDate.now();

        List<ProjectAssignmentItem> content = queryFactory
                .select(Projections.constructor(ProjectAssignmentItem.class,
                        projectAssignment.id,
                        projectAssignment.projectId,
                        projectAssignment.employeeId,
                        employee.name,
                        department.id,
                        department.name,
                        projectAssignment.role.stringValue(),
                        projectAssignment.period.startDate,
                        projectAssignment.period.endDate,
                        assignmentStatusExpression(today)
                ))
                .from(projectAssignment)
                .leftJoin(employee).on(projectAssignment.employeeId.eq(employee.id))
                .leftJoin(department).on(employee.departmentId.eq(department.id))
                .where(
                        projectAssignment.projectId.eq(condition.projectId()),
                        containsEmployeeName(condition.name()),
                        inAssignmentStatuses(condition.assignmentStatuses(), today),
                        inAssignmentRoles(condition.roles()),
                        projectAssignment.deleted.isFalse(),
                        employee.deleted.isFalse()
                )
                .orderBy(projectAssignment.period.startDate.desc(), projectAssignment.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(projectAssignment.count())
                .from(projectAssignment)
                .leftJoin(employee).on(projectAssignment.employeeId.eq(employee.id))
                .where(
                        projectAssignment.projectId.eq(condition.projectId()),
                        containsEmployeeName(condition.name()),
                        inAssignmentStatuses(condition.assignmentStatuses(), today),
                        inAssignmentRoles(condition.roles()),
                        projectAssignment.deleted.isFalse(),
                        employee.deleted.isFalse()
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private @Nullable BooleanExpression containsProjectNameOrCode(@Nullable String name) {
        if (!hasText(name)) {
            return null;
        }

        return project.name.containsIgnoreCase(name)
                .or(project.code.containsIgnoreCase(name));
    }

    private @Nullable BooleanExpression inProjectStatuses(@Nullable List<ProjectStatus> projectStatuses) {
        if (ObjectUtils.isEmpty(projectStatuses)) {
            return null;
        }
        return project.status.in(projectStatuses);
    }

    private @Nullable BooleanExpression inAssignmentStatuses(
            @Nullable List<ProjectAssignmentStatus> assignmentStatuses,
            LocalDate today
    ) {
        if (ObjectUtils.isEmpty(assignmentStatuses)) {
            return null;
        }

        BooleanExpression expression = null;
        for (ProjectAssignmentStatus status : assignmentStatuses) {
            BooleanExpression candidate = switch (status) {
                case CURRENT -> projectAssignment.period.startDate.loe(today)
                        .and(projectAssignment.period.endDate.isNull().or(projectAssignment.period.endDate.goe(today)));
                case SCHEDULED -> projectAssignment.period.startDate.gt(today);
                case ENDED -> projectAssignment.period.endDate.isNotNull().and(projectAssignment.period.endDate.lt(today));
            };

            expression = expression == null ? candidate : expression.or(candidate);
        }

        return expression;
    }

    private @Nullable BooleanExpression inAssignmentRoles(@Nullable List<AssignmentRole> roles) {
        if (ObjectUtils.isEmpty(roles)) {
            return null;
        }
        return projectAssignment.role.in(roles);
    }

    private @Nullable BooleanExpression containsEmployeeName(@Nullable String name) {
        if (!hasText(name)) {
            return null;
        }

        return employee.name.containsIgnoreCase(name);
    }

    private com.querydsl.core.types.Expression<String> assignmentStatusExpression(LocalDate today) {
        return new CaseBuilder()
                .when(projectAssignment.period.startDate.gt(today)).then(ProjectAssignmentStatus.SCHEDULED.name())
                .when(projectAssignment.period.endDate.isNull()
                        .or(projectAssignment.period.endDate.goe(today))).then(ProjectAssignmentStatus.CURRENT.name())
                .otherwise(ProjectAssignmentStatus.ENDED.name());
    }

    private @Nullable BooleanExpression inAccessibleScope(@Nullable CurrentActor actor) {
        if (actor == null) {
            return null;
        }
        if (!actor.hasPermission("project.read")) {
            return project.id.isNull();
        }
        java.util.Set<kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope> scopes = actor.scopesOf("project.read");
        if (scopes.contains(kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope.ALL)) {
            return null;
        }

        java.util.LinkedHashSet<Long> accessibleProjectIds = new java.util.LinkedHashSet<>();
        java.util.LinkedHashSet<Long> accessibleLeadDepartmentIds = new java.util.LinkedHashSet<>();
        if (actor.departmentId() != null
                && scopes.contains(kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope.OWN_DEPARTMENT)) {
            accessibleLeadDepartmentIds.add(actor.departmentId());
        }
        if (actor.departmentId() != null
                && scopes.contains(kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope.OWN_DEPARTMENT_TREE)) {
            accessibleLeadDepartmentIds.addAll(resolveDepartmentTree(actor.departmentId()));
        }
        if (actor.employeeId() != null
                && scopes.contains(kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope.CURRENT_PARTICIPATION)) {
            accessibleProjectIds.addAll(resolveCurrentParticipationProjectIds(actor.employeeId()));
        }

        BooleanExpression projectScope = !accessibleProjectIds.isEmpty()
                ? project.id.in(accessibleProjectIds)
                : null;
        BooleanExpression departmentScope = !accessibleLeadDepartmentIds.isEmpty()
                ? project.leadDepartmentId.in(accessibleLeadDepartmentIds)
                : null;

        if (projectScope == null) {
            return departmentScope != null ? departmentScope : project.id.isNull();
        }
        if (departmentScope == null) {
            return projectScope;
        }
        return projectScope.or(departmentScope);
    }

    private java.util.LinkedHashSet<Long> resolveDepartmentTree(Long rootDepartmentId) {
        java.util.Map<Long, java.util.List<Long>> childrenByParentId = queryFactory
                .select(department.id, department.parent.id)
                .from(department)
                .where(department.deleted.isFalse(), department.parent.id.isNotNull())
                .fetch()
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        tuple -> tuple.get(department.parent.id),
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.mapping(tuple -> tuple.get(department.id), java.util.stream.Collectors.toList())
                ));
        java.util.LinkedHashSet<Long> departmentIds = new java.util.LinkedHashSet<>();
        java.util.ArrayDeque<Long> queue = new java.util.ArrayDeque<>();
        queue.add(rootDepartmentId);
        while (!queue.isEmpty()) {
            Long departmentId = queue.poll();
            if (!departmentIds.add(departmentId)) {
                continue;
            }
            queue.addAll(childrenByParentId.getOrDefault(departmentId, java.util.List.of()));
        }
        return departmentIds;
    }

    private java.util.LinkedHashSet<Long> resolveCurrentParticipationProjectIds(Long employeeId) {
        LocalDate today = LocalDate.now();
        return new java.util.LinkedHashSet<>(queryFactory
                .select(project.id)
                .from(project)
                .join(projectAssignment).on(projectAssignment.projectId.eq(project.id))
                .where(
                        projectAssignment.employeeId.eq(employeeId),
                        projectAssignment.deleted.isFalse(),
                        project.deleted.isFalse(),
                        projectAssignment.period.startDate.loe(today),
                        projectAssignment.period.endDate.isNull().or(projectAssignment.period.endDate.goe(today))
                )
                .fetch());
    }
}
