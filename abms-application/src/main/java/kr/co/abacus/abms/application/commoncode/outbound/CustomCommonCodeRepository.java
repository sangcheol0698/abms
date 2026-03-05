package kr.co.abacus.abms.application.commoncode.outbound;

import java.util.List;

import org.jspecify.annotations.Nullable;

import kr.co.abacus.abms.application.commoncode.dto.CommonCodeInfo;

public interface CustomCommonCodeRepository {

    List<CommonCodeInfo> findDetailsByGroupCode(String groupCode);

    @Nullable CommonCodeInfo findDetailByCode(String groupCode, String code);

}
