package kr.co.abacus.abms.adapter.infrastructure.project;

import static kr.co.abacus.abms.domain.department.QDepartment.*;
import static kr.co.abacus.abms.domain.party.QParty.*;
import static kr.co.abacus.abms.domain.project.QProject.*;
import static org.springframework.util.StringUtils.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.auth.CurrentActor;
import kr.co.abacus.abms.application.project.dto.ProjectOverviewSummary;
import kr.co.abacus.abms.application.project.dto.ProjectSearchCondition;
import kr.co.abacus.abms.application.project.dto.ProjectSummary;
import kr.co.abacus.abms.application.project.outbound.CustomProjectRepository;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectStatus;

@RequiredArgsConstructor
@Repository
public class ProjectRepositoryImpl implements CustomProjectRepository {

    private static final String PROJECT_READ_PERMISSION_CODE = "project.read";
    private static final String PROJECT_EXCEL_DOWNLOAD_PERMISSION_CODE = "project.excel.download";

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProjectSummary> search(ProjectSearchCondition condition, Pageable pageable) {
        return searchInternal(condition, null, pageable);
    }

    @Override
    public Page<ProjectSummary> search(ProjectSearchCondition condition, @Nullable CurrentActor actor, Pageable pageable) {
        return searchInternal(condition, actor, pageable);
    }

    @Override
    public boolean canRead(Long projectId, CurrentActor actor) {
        Integer exists = queryFactory
                .selectOne()
                .from(project)
                .where(
                        project.id.eq(projectId),
                        inAccessibleScope(actor, PROJECT_READ_PERMISSION_CODE),
                        project.deleted.isFalse())
                .fetchFirst();
        return exists != null;
    }

    private Page<ProjectSummary> searchInternal(
            ProjectSearchCondition condition,
            @Nullable CurrentActor actor,
            Pageable pageable
    ) {
        OrderSpecifier<?>[] orderSpecifiers = resolveSort(pageable);

        List<ProjectSummary> content = queryFactory
                .select(Projections.constructor(ProjectSummary.class,
                        project.id,
                        project.partyId,
                        party.name,
                        project.leadDepartmentId,
                        department.name,
                        project.code,
                        project.name,
                        project.description,
                        project.status,
                        project.contractAmount,
                        project.period.startDate,
                        project.period.endDate))
                .from(project)
                .join(party).on(project.partyId.eq(party.id))
                .leftJoin(department).on(project.leadDepartmentId.eq(department.id))
                .where(
                        containsNameOrCode(condition.name()),
                        inStatuses(condition.statuses()),
                        inPartyIds(condition.partyIds()),
                        inAccessibleScope(actor, PROJECT_READ_PERMISSION_CODE),
                        overlapsPeriod(condition.periodStart(), condition.periodEnd()),
                        party.deleted.isFalse(),
                        project.deleted.isFalse())
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(project.count())
                .from(project)
                .join(party).on(project.partyId.eq(party.id))
                .leftJoin(department).on(project.leadDepartmentId.eq(department.id))
                .where(
                        containsNameOrCode(condition.name()),
                        inStatuses(condition.statuses()),
                        inPartyIds(condition.partyIds()),
                        inAccessibleScope(actor, PROJECT_READ_PERMISSION_CODE),
                        overlapsPeriod(condition.periodStart(), condition.periodEnd()),
                        party.deleted.isFalse(),
                        project.deleted.isFalse());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public ProjectOverviewSummary summarize(ProjectSearchCondition condition) {
        return summarize(condition, null);
    }

    @Override
    public ProjectOverviewSummary summarize(ProjectSearchCondition condition, @Nullable CurrentActor actor) {
        return new ProjectOverviewSummary(
                countProjects(condition, actor, PROJECT_READ_PERMISSION_CODE, null),
                countProjects(condition, actor, PROJECT_READ_PERMISSION_CODE, project.status.eq(ProjectStatus.SCHEDULED)),
                countProjects(condition, actor, PROJECT_READ_PERMISSION_CODE, project.status.eq(ProjectStatus.IN_PROGRESS)),
                countProjects(condition, actor, PROJECT_READ_PERMISSION_CODE, project.status.eq(ProjectStatus.COMPLETED)),
                countProjects(condition, actor, PROJECT_READ_PERMISSION_CODE, project.status.eq(ProjectStatus.ON_HOLD)),
                countProjects(condition, actor, PROJECT_READ_PERMISSION_CODE, project.status.eq(ProjectStatus.CANCELLED)),
                sumContractAmount(condition, actor, PROJECT_READ_PERMISSION_CODE));
    }

    @Override
    public List<Project> search(ProjectSearchCondition condition) {
        return searchInternal(condition, null);
    }

    @Override
    public List<Project> search(ProjectSearchCondition condition, @Nullable CurrentActor actor) {
        return searchInternal(condition, actor);
    }

    private List<Project> searchInternal(ProjectSearchCondition condition, @Nullable CurrentActor actor) {
        return queryFactory.select(project)
                .from(project)
                .join(party).on(project.partyId.eq(party.id))
                .where(
                        containsNameOrCode(condition.name()),
                        inStatuses(condition.statuses()),
                        inPartyIds(condition.partyIds()),
                        inAccessibleScope(actor, PROJECT_EXCEL_DOWNLOAD_PERMISSION_CODE),
                        overlapsPeriod(condition.periodStart(), condition.periodEnd()),
                        party.deleted.isFalse(),
                        project.deleted.isFalse())
                .orderBy(defaultSort())
                .fetch();
    }

    private @Nullable BooleanExpression containsNameOrCode(@Nullable String name) {
        if (!hasText(name)) {
            return null;
        }
        return project.name.containsIgnoreCase(name)
                .or(project.code.containsIgnoreCase(name));
    }

    private @Nullable BooleanExpression inStatuses(@Nullable List<ProjectStatus> statuses) {
        if (ObjectUtils.isEmpty(statuses)) {
            return null;
        }
        return project.status.in(statuses);
    }

    private @Nullable BooleanExpression inPartyIds(@Nullable List<Long> partyIds) {
        if (ObjectUtils.isEmpty(partyIds)) {
            return null;
        }
        return project.partyId.in(partyIds);
    }

    private @Nullable BooleanExpression overlapsPeriod(@Nullable LocalDate periodStart, @Nullable LocalDate periodEnd) {
        if (periodStart == null && periodEnd == null) {
            return null;
        }
        if (periodStart != null && periodEnd != null) {
            return project.period.startDate.loe(periodEnd)
                    .and(project.period.endDate.isNull().or(project.period.endDate.goe(periodStart)));
        }
        if (periodStart != null) {
            return project.period.endDate.isNull().or(project.period.endDate.goe(periodStart));
        }
        return project.period.startDate.loe(periodEnd);
    }

    private OrderSpecifier<?>[] resolveSort(Pageable pageable) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        for (Sort.Order order : pageable.getSort()) {
            OrderSpecifier<?> mappedOrder = mapOrder(order);
            orderSpecifiers.add(mappedOrder);
        }

        return orderSpecifiers.toArray(new OrderSpecifier<?>[0]);
    }

    private OrderSpecifier<?> mapOrder(Sort.Order order) {
        Order direction = order.isAscending() ? Order.ASC : Order.DESC;

        return switch (order.getProperty()) {
            case "code" -> new OrderSpecifier<>(direction, project.code);
            case "name" -> new OrderSpecifier<>(direction, project.name);
            case "status" -> new OrderSpecifier<>(direction, project.status);
            case "contractAmount" -> new OrderSpecifier<>(direction, project.contractAmount.amount);
            case "periodStart" -> new OrderSpecifier<>(direction, project.period.startDate);
            case "periodEnd" -> new OrderSpecifier<>(direction, project.period.endDate);
            case "createdAt" -> new OrderSpecifier<>(direction, project.createdAt);
            default -> defaultSort();
        };
    }

    private OrderSpecifier<?> defaultSort() {
        return project.createdAt.desc();
    }

    private long countProjects(
            ProjectSearchCondition condition,
            @Nullable CurrentActor actor,
            String permissionCode,
            @Nullable BooleanExpression extraCondition
    ) {
        Long value = queryFactory
                .select(project.count())
                .from(project)
                .join(party).on(project.partyId.eq(party.id))
                .where(
                        containsNameOrCode(condition.name()),
                        inStatuses(condition.statuses()),
                        inPartyIds(condition.partyIds()),
                        inAccessibleScope(actor, permissionCode),
                        overlapsPeriod(condition.periodStart(), condition.periodEnd()),
                        extraCondition,
                        party.deleted.isFalse(),
                        project.deleted.isFalse())
                .fetchOne();
        return value != null ? value : 0L;
    }

    private long sumContractAmount(
            ProjectSearchCondition condition,
            @Nullable CurrentActor actor,
            String permissionCode
    ) {
        Long value = queryFactory
                .select(Expressions.numberTemplate(Long.class, "coalesce(sum({0}), 0)", project.contractAmount.amount))
                .from(project)
                .join(party).on(project.partyId.eq(party.id))
                .where(
                        containsNameOrCode(condition.name()),
                        inStatuses(condition.statuses()),
                        inPartyIds(condition.partyIds()),
                        inAccessibleScope(actor, permissionCode),
                        overlapsPeriod(condition.periodStart(), condition.periodEnd()),
                        party.deleted.isFalse(),
                        project.deleted.isFalse())
                .fetchOne();
        return value != null ? value : 0L;
    }

    private @Nullable BooleanExpression inAccessibleScope(@Nullable CurrentActor actor, String permissionCode) {
        if (actor == null) {
            return null;
        }
        if (!actor.hasPermission(permissionCode)) {
            return project.id.isNull();
        }
        java.util.Set<kr.co.abacus.abms.domain.grouppermissiongrant.PermissionScope> scopes = actor.scopesOf(permissionCode);
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
        java.time.LocalDate today = java.time.LocalDate.now();
        return new java.util.LinkedHashSet<>(queryFactory
                .select(project.id)
                .from(project)
                .join(kr.co.abacus.abms.domain.projectassignment.QProjectAssignment.projectAssignment)
                .on(kr.co.abacus.abms.domain.projectassignment.QProjectAssignment.projectAssignment.projectId.eq(project.id))
                .where(
                        kr.co.abacus.abms.domain.projectassignment.QProjectAssignment.projectAssignment.employeeId.eq(employeeId),
                        kr.co.abacus.abms.domain.projectassignment.QProjectAssignment.projectAssignment.deleted.isFalse(),
                        project.deleted.isFalse(),
                        kr.co.abacus.abms.domain.projectassignment.QProjectAssignment.projectAssignment.period.startDate.loe(today),
                        kr.co.abacus.abms.domain.projectassignment.QProjectAssignment.projectAssignment.period.endDate.isNull()
                                .or(kr.co.abacus.abms.domain.projectassignment.QProjectAssignment.projectAssignment.period.endDate.goe(today))
                )
                .fetch());
    }

}
