package kr.co.abacus.abms.adapter.infrastructure.party;

import static kr.co.abacus.abms.domain.party.QParty.*;
import static kr.co.abacus.abms.domain.project.QProject.*;

import java.math.BigDecimal;
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
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.party.dto.PartyListItem;
import kr.co.abacus.abms.application.party.dto.PartyOverviewSummary;
import kr.co.abacus.abms.application.party.dto.PartySearchCondition;
import kr.co.abacus.abms.application.party.outbound.CustomPartyRepository;
import kr.co.abacus.abms.domain.project.ProjectStatus;

@RequiredArgsConstructor
@Repository
public class PartyRepositoryImpl implements CustomPartyRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PartyListItem> search(Pageable pageable, PartySearchCondition condition) {
        OrderSpecifier<?>[] orderSpecifiers = resolveSort(pageable);
        List<PartyListItem> content = queryFactory
                .select(Projections.constructor(PartyListItem.class,
                        party.id,
                        party.name,
                        party.ceoName,
                        party.salesRepName,
                        party.salesRepPhone,
                        party.salesRepEmail))
                .from(party)
                .where(
                        party.deleted.isFalse(),
                        containsName(condition.name()))
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(party.count())
                .from(party)
                .where(
                        party.deleted.isFalse(),
                        containsName(condition.name()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public PartyOverviewSummary summarize(PartySearchCondition condition) {
        long normalizedTotalCount = countParties(condition);
        long normalizedWithProjectsCount = countPartiesWithProjects(condition, null);

        return new PartyOverviewSummary(
                normalizedTotalCount,
                normalizedWithProjectsCount,
                countPartiesWithProjects(condition, project.status.eq(ProjectStatus.IN_PROGRESS)),
                Math.max(normalizedTotalCount - normalizedWithProjectsCount, 0L),
                sumContractAmount(condition));
    }

    private @Nullable BooleanExpression containsName(@Nullable String name) {
        if (ObjectUtils.isEmpty(name)) {
            return null;
        }
        return party.name.containsIgnoreCase(name);
    }

    private OrderSpecifier<?>[] resolveSort(Pageable pageable) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        for (Sort.Order order : pageable.getSort()) {
            orderSpecifiers.add(mapOrder(order));
        }

        if (orderSpecifiers.isEmpty()) {
            orderSpecifiers.add(defaultSort());
        }

        return orderSpecifiers.toArray(new OrderSpecifier<?>[0]);
    }

    private OrderSpecifier<?> mapOrder(Sort.Order order) {
        Order direction = order.isAscending() ? Order.ASC : Order.DESC;

        return switch (order.getProperty()) {
            case "name" -> new OrderSpecifier<>(direction, party.name);
            case "ceo" -> new OrderSpecifier<>(direction, party.ceoName);
            case "manager" -> new OrderSpecifier<>(direction, party.salesRepName);
            case "contact" -> new OrderSpecifier<>(direction, party.salesRepPhone);
            case "email" -> new OrderSpecifier<>(direction, party.salesRepEmail);
            case "createdAt" -> new OrderSpecifier<>(direction, party.createdAt);
            default -> defaultSort();
        };
    }

    private OrderSpecifier<?> defaultSort() {
        return party.createdAt.desc();
    }

    private long countParties(PartySearchCondition condition) {
        Long value = queryFactory
                .select(party.count())
                .from(party)
                .where(
                        party.deleted.isFalse(),
                        containsName(condition.name()))
                .fetchOne();
        return value != null ? value : 0L;
    }

    private long countPartiesWithProjects(PartySearchCondition condition, @Nullable BooleanExpression projectCondition) {
        Long value = queryFactory
                .select(party.id.countDistinct())
                .from(party)
                .join(project).on(project.partyId.eq(party.id))
                .where(
                        party.deleted.isFalse(),
                        containsName(condition.name()),
                        project.deleted.isFalse(),
                        projectCondition)
                .fetchOne();
        return value != null ? value : 0L;
    }

    private long sumContractAmount(PartySearchCondition condition) {
        Long value = queryFactory
                .select(Expressions.numberTemplate(Long.class, "coalesce(sum({0}), 0)", project.contractAmount.amount))
                .from(project)
                .join(party).on(project.partyId.eq(party.id))
                .where(
                        project.deleted.isFalse(),
                        party.deleted.isFalse(),
                        containsName(condition.name()))
                .fetchOne();
        return value != null ? value : 0L;
    }

}
