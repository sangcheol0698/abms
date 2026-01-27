package kr.co.abacus.abms.adapter.infrastructure.party;

import static kr.co.abacus.abms.domain.party.QParty.*;

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
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.party.outbound.CustomPartyRepository;
import kr.co.abacus.abms.domain.party.Party;

@RequiredArgsConstructor
@Repository
public class PartyRepositoryImpl implements CustomPartyRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Party> search(Pageable pageable, String name) {
        OrderSpecifier<?>[] orderSpecifiers = resolveSort(pageable);
        List<Party> content = queryFactory
                .selectFrom(party)
                .where(
                        party.deleted.isFalse(),
                        containsName(name))
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(party.count())
                .from(party)
                .where(
                        party.deleted.isFalse(),
                        containsName(name));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private @Nullable BooleanExpression containsName(String name) {
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

}
