package kr.co.abacus.abms.application.commoncode.outbound;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import kr.co.abacus.abms.domain.commoncode.CommonCodeDetail;
import kr.co.abacus.abms.domain.commoncode.CommonCodeDetailId;

public interface CommonCodeDetailRepository extends Repository<CommonCodeDetail, CommonCodeDetailId> {

    @Query("SELECT cd " +
            "FROM CommonCodeDetail cd " +
            "WHERE cd.id.groupCode = :groupCode AND cd.deleted = false")
    List<CommonCodeDetail> findByGroupCode(String groupCode);

    @Query("SELECT cd " +
            "FROM CommonCodeDetail cd " +
            "WHERE cd.id.groupCode = :groupCode AND cd.id.code = :code AND cd.deleted = false")
    @Nullable CommonCodeDetail findByCode(String groupCode, String code);

}
