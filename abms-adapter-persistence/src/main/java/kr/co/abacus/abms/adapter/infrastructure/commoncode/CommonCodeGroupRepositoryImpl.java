package kr.co.abacus.abms.adapter.infrastructure.commoncode;

import static kr.co.abacus.abms.domain.commoncode.QCommonCodeDetail.*;
import static kr.co.abacus.abms.domain.commoncode.QCommonCodeGroup.*;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import kr.co.abacus.abms.application.commoncode.dto.CommonCodeInfo;
import kr.co.abacus.abms.application.commoncode.outbound.CustomCommonCodeRepository;

@RequiredArgsConstructor
@Repository
public class CommonCodeGroupRepositoryImpl implements CustomCommonCodeRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<CommonCodeInfo> findDetailsByGroupCode(String groupCode) {
        return queryFactory
                .select(Projections.constructor(CommonCodeInfo.class,
                        commonCodeDetail.id.groupCode,
                        commonCodeDetail.id.code,
                        commonCodeDetail.name,
                        commonCodeDetail.sortOrder))
                .from(commonCodeDetail)
                .join(commonCodeDetail.group, commonCodeGroup)
                .where(
                        commonCodeDetail.id.groupCode.eq(groupCode),
                        commonCodeDetail.deleted.isFalse(),
                        commonCodeGroup.deleted.isFalse())
                .orderBy(commonCodeDetail.sortOrder.asc(), commonCodeDetail.id.code.asc())
                .fetch();
    }

    @Override
    public @Nullable CommonCodeInfo findDetailByCode(String groupCode, String code) {
        return queryFactory
                .select(Projections.constructor(CommonCodeInfo.class,
                        commonCodeDetail.id.groupCode,
                        commonCodeDetail.id.code,
                        commonCodeDetail.name,
                        commonCodeDetail.sortOrder))
                .from(commonCodeDetail)
                .join(commonCodeDetail.group, commonCodeGroup)
                .where(
                        commonCodeDetail.id.groupCode.eq(groupCode),
                        commonCodeDetail.id.code.eq(code),
                        commonCodeDetail.deleted.isFalse(),
                        commonCodeGroup.deleted.isFalse())
                .fetchOne();
    }

}
