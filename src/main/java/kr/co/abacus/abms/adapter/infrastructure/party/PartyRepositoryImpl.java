package kr.co.abacus.abms.adapter.infrastructure.party;

import static kr.co.abacus.abms.domain.party.QParty.*;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

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
        List<Party> content = queryFactory
            .selectFrom(party)
            .where(containsName(name))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(party.count())
            .from(party)
            .where(containsName(name));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private @Nullable BooleanExpression containsName(String name) {
        if (ObjectUtils.isEmpty(name)) {
            return null;
        }
        return party.name.containsIgnoreCase(name);
    }

}
