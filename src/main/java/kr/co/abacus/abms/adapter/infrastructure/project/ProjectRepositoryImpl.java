package kr.co.abacus.abms.adapter.infrastructure.project;

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
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.project.dto.ProjectSearchCondition;
import kr.co.abacus.abms.application.project.dto.ProjectSummary;
import kr.co.abacus.abms.application.project.outbound.CustomProjectRepository;
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
                        project.code,
                        project.name,
                        project.description,
                        project.status,
                        project.contractAmount,
                        project.period.startDate,
                        project.period.endDate))
                .from(project)
                .where(
                        containsNameOrCode(condition.name()),
                        inStatuses(condition.statuses()),
                        inPartyIds(condition.partyIds()),
                        startDateFrom(condition.startDate()),
                        startDateTo(condition.endDate()),
                        project.deleted.isFalse())
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(project.count())
                .from(project)
                .where(
                        containsNameOrCode(condition.name()),
                        inStatuses(condition.statuses()),
                        inPartyIds(condition.partyIds()),
                        startDateFrom(condition.startDate()),
                        startDateTo(condition.endDate()),
                        project.deleted.isFalse());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
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

    private @Nullable BooleanExpression startDateFrom(@Nullable LocalDate startDate) {
        return startDate != null ? project.period.startDate.goe(startDate) : null;
    }

    private @Nullable BooleanExpression startDateTo(@Nullable LocalDate endDate) {
        return endDate != null ? project.period.startDate.loe(endDate) : null;
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
            case "startDate" -> new OrderSpecifier<>(direction, project.period.startDate);
            case "endDate" -> new OrderSpecifier<>(direction, project.period.endDate);
            case "createdAt" -> new OrderSpecifier<>(direction, project.createdAt);
            default -> defaultSort();
        };
    }

    private OrderSpecifier<?> defaultSort() {
        return project.createdAt.desc();
    }

}
