package kr.co.abacus.abms.adapter.infrastructure.project;

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

import kr.co.abacus.abms.application.project.dto.ProjectOverviewSummary;
import kr.co.abacus.abms.application.project.dto.ProjectSearchCondition;
import kr.co.abacus.abms.application.project.dto.ProjectSummary;
import kr.co.abacus.abms.application.project.outbound.CustomProjectRepository;
import kr.co.abacus.abms.domain.project.Project;
import kr.co.abacus.abms.domain.project.ProjectStatus;

@RequiredArgsConstructor
@Repository
public class ProjectRepositoryImpl implements CustomProjectRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProjectSummary> search(ProjectSearchCondition condition, Pageable pageable) {
        OrderSpecifier<?>[] orderSpecifiers = resolveSort(pageable);

        List<ProjectSummary> content = queryFactory
                .select(Projections.constructor(ProjectSummary.class,
                        project.id,
                        project.partyId,
                        party.name,
                        project.code,
                        project.name,
                        project.description,
                        project.status,
                        project.contractAmount,
                        project.period.startDate,
                        project.period.endDate))
                .from(project)
                .join(party).on(project.partyId.eq(party.id))
                .where(
                        containsNameOrCode(condition.name()),
                        inStatuses(condition.statuses()),
                        inPartyIds(condition.partyIds()),
                        overlapsPeriod(condition.periodStart(), condition.periodEnd()),
                        project.deleted.isFalse())
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(project.count())
                .from(project)
                .join(party).on(project.partyId.eq(party.id))
                .where(
                        containsNameOrCode(condition.name()),
                        inStatuses(condition.statuses()),
                        inPartyIds(condition.partyIds()),
                        overlapsPeriod(condition.periodStart(), condition.periodEnd()),
                        project.deleted.isFalse());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public ProjectOverviewSummary summarize(ProjectSearchCondition condition) {
        return new ProjectOverviewSummary(
                countProjects(condition, null),
                countProjects(condition, project.status.eq(ProjectStatus.SCHEDULED)),
                countProjects(condition, project.status.eq(ProjectStatus.IN_PROGRESS)),
                countProjects(condition, project.status.eq(ProjectStatus.COMPLETED)),
                countProjects(condition, project.status.eq(ProjectStatus.ON_HOLD)),
                countProjects(condition, project.status.eq(ProjectStatus.CANCELLED)),
                sumContractAmount(condition));
    }

    @Override
    public List<Project> search(ProjectSearchCondition condition) {
        return queryFactory.selectFrom(project)
                .where(
                        containsNameOrCode(condition.name()),
                        inStatuses(condition.statuses()),
                        inPartyIds(condition.partyIds()),
                        overlapsPeriod(condition.periodStart(), condition.periodEnd()),
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

    private long countProjects(ProjectSearchCondition condition, @Nullable BooleanExpression extraCondition) {
        Long value = queryFactory
                .select(project.count())
                .from(project)
                .where(
                        containsNameOrCode(condition.name()),
                        inStatuses(condition.statuses()),
                        inPartyIds(condition.partyIds()),
                        overlapsPeriod(condition.periodStart(), condition.periodEnd()),
                        extraCondition,
                        project.deleted.isFalse())
                .fetchOne();
        return value != null ? value : 0L;
    }

    private long sumContractAmount(ProjectSearchCondition condition) {
        Long value = queryFactory
                .select(Expressions.numberTemplate(Long.class, "coalesce(sum({0}), 0)", project.contractAmount.amount))
                .from(project)
                .where(
                        containsNameOrCode(condition.name()),
                        inStatuses(condition.statuses()),
                        inPartyIds(condition.partyIds()),
                        overlapsPeriod(condition.periodStart(), condition.periodEnd()),
                        project.deleted.isFalse())
                .fetchOne();
        return value != null ? value : 0L;
    }

}
