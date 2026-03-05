package kr.co.abacus.abms.application.commoncode.outbound;

import java.util.Optional;

import kr.co.abacus.abms.domain.commoncode.CommonCodeGroup;

public interface CommonCodeGroupRepository extends CustomCommonCodeRepository {

    CommonCodeGroup save(CommonCodeGroup group);

    Optional<CommonCodeGroup> findByGroupCode(String groupCode);

}
