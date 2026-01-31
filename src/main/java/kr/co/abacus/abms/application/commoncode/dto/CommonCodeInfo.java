package kr.co.abacus.abms.application.commoncode.dto;
import kr.co.abacus.abms.domain.commoncode.CommonCodeDetail;

public record CommonCodeInfo(
        String groupCode,
        String code,
        String name,
        int sortOrder
) {
    public static CommonCodeInfo from(CommonCodeDetail detail) {
        return new CommonCodeInfo(
                detail.getId().getGroupCode(),
                detail.getId().getCode(),
                detail.getName(),
                detail.getSortOrder()
        );
    }
}
