package kr.co.abacus.abms.application.commoncode.outbound;

import java.util.List;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.domain.commoncode.CommonCodeDetail;
import kr.co.abacus.abms.domain.commoncode.CommonCodeDetailId;

public interface CommonCodeDetailRepository {

    List<CommonCodeDetail> findByGroupCode(String groupCode);

    @Nullable CommonCodeDetail findByCode(String groupCode, String code);

}
