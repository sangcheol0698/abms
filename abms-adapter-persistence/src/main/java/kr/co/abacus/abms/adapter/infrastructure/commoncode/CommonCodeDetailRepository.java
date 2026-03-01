package kr.co.abacus.abms.adapter.infrastructure.commoncode;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.commoncode.CommonCodeDetail;
import kr.co.abacus.abms.domain.commoncode.CommonCodeDetailId;

public interface CommonCodeDetailRepository
        extends Repository<CommonCodeDetail, CommonCodeDetailId>,
        kr.co.abacus.abms.application.commoncode.outbound.CommonCodeDetailRepository {

    @Override
    @Query("SELECT cd " +
            "FROM CommonCodeDetail cd " +
            "WHERE cd.id.groupCode = :groupCode AND cd.deleted = false")
    java.util.List<CommonCodeDetail> findByGroupCode(String groupCode);

    @Override
    @Query("SELECT cd " +
            "FROM CommonCodeDetail cd " +
            "WHERE cd.id.groupCode = :groupCode AND cd.id.code = :code AND cd.deleted = false")
    @Nullable CommonCodeDetail findByCode(String groupCode, String code);
}
